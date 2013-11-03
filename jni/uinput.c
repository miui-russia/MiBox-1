#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <linux/input.h>
#include <jni.h>
#include "uinput.h"

static int s_uinput_fd = 0;
static struct uinput_user_dev s_uidev;

JNIEXPORT jint JNICALL Java_com_sony_mibox_MiBoxService_uinput_init(JNIEnv *evn, jobject obj)
{
    s_uinput_fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
    if (s_uinput_fd < 0)
    {
        perror("open uinput device error");
        return -1;
    }

    ioctl(s_uinput_fd, UI_SET_EVBIT, EV_KEY);
    ioctl(s_uinput_fd, UI_SET_KEYBIT, KEY_LEFT);
    ioctl(s_uinput_fd, UI_SET_KEYBIT, KEY_RIGHT);
    ioctl(s_uinput_fd, UI_SET_KEYBIT, KEY_UP);
    ioctl(s_uinput_fd, UI_SET_KEYBIT, KEY_DOWN);
    ioctl(s_uinput_fd, UI_SET_KEYBIT, KEY_HOME);
    ioctl(s_uinput_fd, UI_SET_KEYBIT, KEY_BACK);
    ioctl(s_uinput_fd, UI_SET_KEYBIT, KEY_ENTER);

    memset(&s_uidev, 0, sizeof(s_uidev));
    snprintf(s_uidev.name, UINPUT_MAX_NAME_SIZE, "mibox_key_injector");
    s_uidev.id.bustype = BUS_USB;
    s_uidev.id.vendor = 0x1;
    s_uidev.id.product = 0x1;
    s_uidev.id.version = 1;

    write(s_uinput_fd, &s_uidev, sizeof(s_uidev));
    ioctl(s_uinput_fd, UI_DEV_CREATE);

    return 0;
}

JNIEXPORT void JNICALL Java_com_sony_mibox_MiBoxService_uinput_destroy(JNIEnv *env, jobject obj)
{
    ioctl(s_uinput_fd, UI_DEV_DESTROY);
    close(s_uinput_fd);
}

JNIEXPORT jint JNICALL Java_com_sony_mibox_MiBoxService_uinput_inject_key(JNIEnv *env, jobject obj, jint keycode)
{
    struct input_event event;

    if (s_uinput_fd > 0)
    {
        memset(&event, 0, sizeof(event));
        event.type = EV_KEY;
        event.code = keycode;
        event.value = 1; //press
        write(s_uinput_fd, &event, sizeof(event));

        memset(&event, 0, sizeof(event));
        event.type = EV_KEY;
        event.code = keycode;
        event.value = 0; //release
        write(s_uinput_fd, &event, sizeof(event));

        memset(&event, 0, sizeof(event));
        event.type = EV_SYN;
        event.code = 0;
        event.value = 0;
        write(s_uinput_fd, &event, sizeof(event));

        usleep(15000);
        return 0;
    }

    return -1;
}
