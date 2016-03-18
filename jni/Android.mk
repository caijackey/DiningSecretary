# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := chk
LOCAL_SRC_FILES := chk.c  
LOCAL_C_INCLUDES+= /platforms/android-8/arch-arm/usr/include/
LOCAL_LDLIBS    += -L/platforms/android-8/arch-arm/usr/lib/ -llog
LOCAL_LDLIBS    += -L/platforms/android-8/arch-arm/usr/lib/ -lz
LOCAL_SHARED_LIBRARIES := libzip
include $(BUILD_SHARED_LIBRARY)

# Add prebuilt libzip
include $(CLEAR_VARS)
LOCAL_MODULE := libzip
LOCAL_SRC_FILES := libzip.so
include $(PREBUILT_SHARED_LIBRARY)

# Add prebuilt libBMapApiEngine
include $(CLEAR_VARS)
LOCAL_MODULE := libapp_BaiduMapApplib_v2_1_2
LOCAL_SRC_FILES := libapp_BaiduMapApplib_v2_1_2.so
include $(PREBUILT_SHARED_LIBRARY)

# Add prebuilt liblocSDK3.so
include $(CLEAR_VARS)
LOCAL_MODULE := liblocSDK4d
LOCAL_SRC_FILES := liblocSDK4d.so
include $(PREBUILT_SHARED_LIBRARY)

# Add prebuilt libvi_voslib.so
include $(CLEAR_VARS)
LOCAL_MODULE := libvi_voslib
LOCAL_SRC_FILES := libvi_voslib.so
include $(PREBUILT_SHARED_LIBRARY)

# Add prebuilt libmp3lame
include $(CLEAR_VARS)
LOCAL_MODULE := libmp3lame
LOCAL_SRC_FILES := libmp3lame.so
include $(PREBUILT_SHARED_LIBRARY)