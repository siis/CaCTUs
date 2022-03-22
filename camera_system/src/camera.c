#include "camera.h"

int counterFrames = 0;

void errno_exit(const char *s) {
  fprintf(stderr, "%s error %d, %s\\n", s, errno, strerror(errno));
  exit(EXIT_FAILURE);
}

int xioctl(int fh, int request, void *arg) {
  int r;
  do {
    r = ioctl(fh, request, arg);
  } while (-1 == r && EINTR == errno);
  return r;
}

void open_device(char *dev_name, int *fd) {
  struct stat st;

  if (-1 == stat(dev_name, &st)) {
    fprintf(stderr, "Cannot identify '%s': %d, %s\\n", dev_name, errno,
            strerror(errno));
    exit(EXIT_FAILURE);
  }

  if (!S_ISCHR(st.st_mode)) {
    fprintf(stderr, "%s is no device", dev_name);
    exit(EXIT_FAILURE);
  }

  (*fd) = open(dev_name, O_RDWR /* required */ | O_NONBLOCK, 0);

  if (-1 == (*fd)) {
    fprintf(stderr, "Cannot open '%s': %d, %s\\n", dev_name, errno,
            strerror(errno));
    exit(EXIT_FAILURE);
  }
}

void init_device(char *dev_name, int *fd, int frame_width, int frame_height,
                 int frame_rate, struct buffer **buffers,
                 unsigned int *n_buffers) {
  struct v4l2_capability cap;
  struct v4l2_cropcap cropcap;
  struct v4l2_crop crop;
  struct v4l2_format fmt;
  struct v4l2_streamparm param;
  unsigned int min;

  if (-1 == xioctl(*fd, VIDIOC_QUERYCAP, &cap)) {
    if (EINVAL == errno) {
      fprintf(stderr, "%s is no V4L2 device\\n", dev_name);
      exit(EXIT_FAILURE);
    } else {
      errno_exit("VIDIOC_QUERYCAP");
    }
  }

  if (!(cap.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
    fprintf(stderr, "%s is no video capture device\\n", dev_name);
    exit(EXIT_FAILURE);
  }

  if (!(cap.capabilities & V4L2_CAP_STREAMING)) {
    fprintf(stderr, "%s does not support streaming i/o\\n", dev_name);
    exit(EXIT_FAILURE);
  }

  /* Select video input, video standard and tune here. */
  CLEAR(cropcap);
  cropcap.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

  if (0 == xioctl(*fd, VIDIOC_CROPCAP, &cropcap)) {
    crop.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    crop.c = cropcap.defrect; /* reset to default */

    if (-1 == xioctl(*fd, VIDIOC_S_CROP, &crop)) {
      switch (errno) {
      case EINVAL:
        /* Cropping not supported. */
        break;
      default:
        /* Errors ignored. */
        break;
      }
    }
  } else {
    /* Errors ignored. */
  }

  CLEAR(param);
  param.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
  param.parm.capture.timeperframe.numerator = 1;
  param.parm.capture.timeperframe.denominator = frame_rate;
  if (-1 == xioctl(*fd, VIDIOC_S_PARM, &param)) {
    errno_exit("VIDIOC_S_PARM");
  }

  if (param.parm.capture.timeperframe.numerator) {
    double fps_new = param.parm.capture.timeperframe.denominator /
                     param.parm.capture.timeperframe.numerator;
    if ((double)frame_rate != fps_new) {
      printf("unsupported frame rate [%d,%f]\n", frame_rate, fps_new);
      return;
    }
    // else{
    // 	// printf("new fps:%u , %u/%u\n",frame_rate,
    // param.parm.capture.timeperframe.denominator,
    // 	// param.parm.capture.timeperframe.numerator);
    // }
  }

  CLEAR(fmt);

  fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
  fmt.fmt.pix.width = frame_width;
  fmt.fmt.pix.height = frame_height;
  fmt.fmt.pix.pixelformat = V4L2_PIX_FMT_MJPEG;
  fmt.fmt.pix.field = V4L2_FIELD_NONE;

  if (-1 == xioctl(*fd, VIDIOC_S_FMT, &fmt))
    errno_exit("VIDIOC_S_FMT");

  /* Note VIDIOC_S_FMT may change width and height. */

  /* Buggy driver paranoia. */
  min = fmt.fmt.pix.width * 2;
  if (fmt.fmt.pix.bytesperline < min)
    fmt.fmt.pix.bytesperline = min;
  min = fmt.fmt.pix.bytesperline * fmt.fmt.pix.height;
  if (fmt.fmt.pix.sizeimage < min)
    fmt.fmt.pix.sizeimage = min;

  init_mmap(dev_name, fd, buffers, n_buffers);
}

void start_capturing(int *fd, unsigned int n_buffers) {
  unsigned int i;
  enum v4l2_buf_type type;
  for (i = 0; i < n_buffers; ++i) {
    struct v4l2_buffer buf;

    CLEAR(buf);
    buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    buf.memory = V4L2_MEMORY_MMAP;
    buf.index = i;

    if (-1 == xioctl(*fd, VIDIOC_QBUF, &buf))
      errno_exit("VIDIOC_QBUF");
  }
  type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
  if (-1 == xioctl(*fd, VIDIOC_STREAMON, &type))
    errno_exit("VIDIOC_STREAMON");
}

void mainloop(int *fd, bool *recording, unsigned int n_buffers,
              struct buffer **buffers, char *upload_url, key_tree *tree,
              EVP_PKEY *pkey, setup_ret_args_t *ret_args) {
  while (*recording) {
    fd_set fds;
    struct timeval tv;
    int r;

    FD_ZERO(&fds);
    FD_SET(*fd, &fds);

    /* Timeout. */
    tv.tv_sec = 2;
    tv.tv_usec = 0;

    r = select(*fd + 1, &fds, NULL, NULL, &tv);

    if (-1 == r) {
      if (EINTR == errno)
        continue;
      errno_exit("select");
    }

    if (0 == r) {
      fprintf(stderr, "select timeout\\n");
      exit(EXIT_FAILURE);
    }

    if (read_frame(fd, n_buffers, buffers, recording, upload_url, tree, pkey,
                   ret_args) == EXIT_FAILURE)
      break;
  }
}

void stop_capturing(int *fd) {
  enum v4l2_buf_type type;
  type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
  if (-1 == xioctl(*fd, VIDIOC_STREAMOFF, &type))
    errno_exit("VIDIOC_STREAMOFF");
}

void close_device(int *fd) {
  if (-1 == close(*fd))
    errno_exit("close");

  (*fd) = -1;
}

void init_mmap(char *dev_name, int *fd, struct buffer **buffers,
               unsigned int *n_buffers) {
  struct v4l2_requestbuffers req;

  CLEAR(req);

  req.count = 4;
  req.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
  req.memory = V4L2_MEMORY_MMAP;

  if (-1 == xioctl(*fd, VIDIOC_REQBUFS, &req)) {
    if (EINVAL == errno) {
      fprintf(stderr, "%s does not support memory mapping", dev_name);
      exit(EXIT_FAILURE);
    } else {
      errno_exit("VIDIOC_REQBUFS");
    }
  }

  if (req.count < 2) {
    fprintf(stderr, "Insufficient buffer memory on %s\\n", dev_name);
    exit(EXIT_FAILURE);
  }

  *buffers = calloc(req.count, sizeof(**buffers));

  if (!(*buffers)) {
    fprintf(stderr, "Out of memory\\n");
    exit(EXIT_FAILURE);
  }

  for ((*n_buffers) = 0; (*n_buffers) < req.count; ++(*n_buffers)) {
    struct v4l2_buffer buf;

    CLEAR(buf);

    buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    buf.memory = V4L2_MEMORY_MMAP;
    buf.index = *n_buffers;

    if (-1 == xioctl(*fd, VIDIOC_QUERYBUF, &buf))
      errno_exit("VIDIOC_QUERYBUF");

    (*buffers)[*n_buffers].length = buf.length;
    (*buffers)[*n_buffers].start =
        mmap(NULL /* start anywhere */, buf.length,
             PROT_READ | PROT_WRITE /* required */,
             MAP_SHARED /* recommended */, *fd, buf.m.offset);

    if (MAP_FAILED == (*buffers)[*n_buffers].start)
      errno_exit("mmap");
  }
}

void uninit_device(unsigned int n_buffers, struct buffer **buffers) {
  unsigned int i;

  for (i = 0; i < n_buffers; ++i)
    if (-1 == munmap((*buffers)[i].start, (*buffers)[i].length))
      errno_exit("munmap");

  free(*buffers);
}

int read_frame(int *fd, unsigned int n_buffers, struct buffer **buffers,
               bool *recording, char *upload_url, key_tree *tree,
               EVP_PKEY *pkey, setup_ret_args_t *ret_args) {
  struct v4l2_buffer buf;
  unsigned int i;

  CLEAR(buf);

  buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
  buf.memory = V4L2_MEMORY_MMAP;

  if (-1 == xioctl(*fd, VIDIOC_DQBUF, &buf)) {
    switch (errno) {
    case EAGAIN:
      return EXIT_FAILURE;
    case EIO:
      /* Could ignore EIO, see spec. */
      /* fall through */
    default:
      errno_exit("VIDIOC_DQBUF");
    }
  }

  assert(buf.index < n_buffers);

  if (ret_args && ret_args->do_setup) {
    // if doing setup, just get the frame, dont need to write it to file
    ret_args->buf_bytesused = buf.bytesused;
    ret_args->buf_frame_data = calloc(ret_args->buf_bytesused, 1);
    memcpy(ret_args->buf_frame_data, (*buffers)[buf.index].start,
           ret_args->buf_bytesused); // safe to enq buffer again now
    *recording = false; // stop capture for now and try to decode qr code
  } else {
    process_image((*buffers)[buf.index].start, buf.bytesused, recording,
                  upload_url, tree, pkey);
  }

  if (-1 == xioctl(*fd, VIDIOC_QBUF, &buf))
    errno_exit("VIDIOC_QBUF");
  return EXIT_SUCCESS;
}

void process_image(const void *p, int size, bool *recording, char *upload_url,
                   key_tree *tree, EVP_PKEY *pkey) {
  unsigned long long current_time = get_current_time_in_milliseconds();
  char filename[100];
  char filepath[100];
  sprintf(filename, "%llu", current_time);
  sprintf(filepath, "img/%llu", current_time);

  unsigned char cipher[(size / 16 + 1) * 16]; // padding block of size 16
  int cipher_len;
  unsigned char *key;
  key = NULL;
  if (!find_key_for_timestamp(current_time, tree, false, &key)) {
    return;
  }
#ifdef PERF
  unsigned long long time_after_key_extraction =
      get_current_time_in_milliseconds();
#endif

  // EVP Authenticated Encryption using GCM mode
  int iv_len = 16;
  unsigned char iv[iv_len];
  generateRandBytes(iv_len, iv);
  int tag_len = 16;
  unsigned char tag[tag_len];
  cipher_len =
      gcm_encrypt((unsigned char *)p, size, (unsigned char *)&current_time,
                  sizeof(unsigned long long), key, iv, iv_len, cipher, tag);
#ifdef PERF
  unsigned long long time_after_encrypt = get_current_time_in_milliseconds();
#endif

  // Signature (every frame is signed consider it as the worst-case scenario,
  // as signing per block would require more modifications to the code)
  int sig_len = 256;
  unsigned char sig[sig_len];
  sig_len = sign_rsa(tag, tag_len, sig, pkey);
#ifdef PERF
  unsigned long long time_after_signing = get_current_time_in_milliseconds();
#endif

  // write cipherdata + iv + tag + sign
  FILE *fp = fopen(filepath, "wb");
  fwrite(cipher, cipher_len, 1, fp); // add cipher
  fwrite(iv, iv_len, 1, fp);         // add iv
  fwrite(tag, tag_len, 1, fp);       // add tag
  fwrite(sig, sig_len, 1, fp);       // add sign
  fflush(fp);
  fclose(fp);
#ifdef PERF
  unsigned long long time_after_writing = get_current_time_in_milliseconds();
#endif

  OPENSSL_cleanse(iv, iv_len);
  OPENSSL_cleanse(tag, tag_len);
  OPENSSL_cleanse(cipher, cipher_len);
  OPENSSL_cleanse(sig, sig_len);

  char transfer_command[300];
  sprintf(transfer_command, "./transfer_file_http_post.sh %s %s %s &", filepath,
          filename, upload_url);
  if (!system(transfer_command)) {
    /* Errors ignored */
  }

#ifdef PERF
  FILE *f = fopen("camera.csv", "a");
  fprintf(f, "%llu,%llu,%llu,%llu,%llu\n", current_time,
          time_after_key_extraction, time_after_encrypt, time_after_signing,
          time_after_writing);
  fclose(f);
#endif

  counterFrames += 1;
  if (counterFrames >= 1000) {
    *recording = false; // to stop the capture after some moment (for
                        // test/evaluation purposes)
  }
}