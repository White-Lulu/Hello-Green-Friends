<template>
  <div class="settings-container">
    <h1 class="page-title">个人设置</h1>

    <div v-if="!authStore.user" class="loading-pane">
      <div class="spinner"></div>
    </div>

    <div v-else class="settings-content">
      <section class="settings-section">
        <h2 class="section-title">个人资料</h2>
        <div class="profile-card">
          <div class="avatar-display">
            <img
              :src="
                avatarPreviewUrl ||
                authStore.user.avatarUrl ||
                '/images/default-avatar.png'
              "
              alt="用户头像"
              class="current-avatar"
            />
          </div>
          <div class="info-fields">
            <div class="field">
              <label for="username">账号</label>
              <input
                id="username"
                type="text"
                :value="authStore.user.username"
                readonly
              />
            </div>
            <div class="field">
              <label for="nickname">昵称</label>
              <div class="editable-field">
                <input
                  id="nickname"
                  type="text"
                  v-model="editableNickname"
                  :readonly="!isEditingNickname"
                  :class="{ 'is-editing': isEditingNickname }"
                />
                <div class="edit-actions" v-if="!isEditingNickname">
                  <button @click="startEditNickname" class="button-edit">
                    编辑
                  </button>
                </div>
                <div class="edit-actions" v-else>
                  <button
                    @click="saveNickname"
                    class="button-save"
                    :disabled="isUpdatingNickname"
                  >
                    {{ isUpdatingNickname ? '保存中...' : '保存' }}
                  </button>
                  <button @click="cancelEditNickname" class="button-cancel">
                    取消
                  </button>
                </div>
              </div>
            </div>
            <div class="field">
              <label for="email">邮箱</label>
              <input
                id="email"
                type="text"
                :value="authStore.user.email"
                readonly
              />
            </div>
          </div>
        </div>
      </section>

      <section class="settings-section">
        <h2 class="section-title">更换头像</h2>
        <div class="form-card">
          <p>选择一张新图片作为你的头像</p>
          <div class="avatar-upload-area">
            <input
              type="file"
              @change="handleFileSelect"
              accept="image/*"
              ref="fileInput"
              style="display: none"
            />
            <button @click="triggerFileInput" class="button-outline">
              选择图片
            </button>
            <span v-if="avatarFile" class="file-name">{{
              avatarFile.name
            }}</span>
          </div>
          <button
            @click="uploadAvatar"
            :disabled="!avatarFile || isUploadingAvatar"
            class="button-primary"
          >
            {{ isUploadingAvatar ? '上传中...' : '保存新头像' }}
          </button>
        </div>
      </section>

      <section class="settings-section">
        <h2 class="section-title">修改密码</h2>
        <div class="form-card">
          <div class="field">
            <label for="old-password">当前密码</label>
            <input
              id="old-password"
              type="password"
              v-model="passwordForm.oldPassword"
              placeholder="请输入当前使用的密码"
            />
          </div>
          <div class="field">
            <label for="new-password">新密码</label>
            <input
              id="new-password"
              type="password"
              v-model="passwordForm.newPassword"
              placeholder="请输入新密码"
            />
          </div>
          <div class="field">
            <label for="confirm-password">确认新密码</label>
            <input
              id="confirm-password"
              type="password"
              v-model="passwordForm.confirmNewPassword"
              placeholder="请再次输入新密码"
            />
          </div>
          <button
            @click="changePassword"
            :disabled="isChangingPassword"
            class="button-primary"
          >
            {{ isChangingPassword ? '正在更改...' : '确认更改密码' }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from 'vue';
import { useAuthStore } from '@/stores/auth';
import apiClient from '@/services/apiClient';

const authStore = useAuthStore();
const fileInput = ref(null);
const avatarFile = ref(null);
const avatarPreviewUrl = ref('');
const isUploadingAvatar = ref(false);


const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: '',
});

const isChangingPassword = ref(false);
const isEditingNickname = ref(false);
const isUpdatingNickname = ref(false);
const editableNickname = ref('');

let originalNickname = '';

watchEffect(() => {
  if (authStore.user) {
    editableNickname.value = authStore.user.nickname;
    originalNickname = authStore.user.nickname;
  }
});
const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    avatarFile.value = file;
    avatarPreviewUrl.value = URL.createObjectURL(file);
  }
};

const uploadAvatar = async () => {
  if (!avatarFile.value) return;

  isUploadingAvatar.value = true;
  const formData = new FormData();
  formData.append('file', avatarFile.value);

  try {
    const response = await apiClient.put('/users/me/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    if (response.data.success) {
      authStore.user = response.data.data;
      alert('头像更新成功！');
      avatarFile.value = null;
      avatarPreviewUrl.value = '';
    } else {
      alert('头像更新失败: ' + (response.data.message || '未知错误'));
    }
  } catch (error) {
    alert('头像上传失败: ' + (error.response?.data?.message || error.message));
  } finally {
    isUploadingAvatar.value = false;
  }
};

const startEditNickname = () => {
  isEditingNickname.value = true;
};

const cancelEditNickname = () => {
  editableNickname.value = originalNickname;
  isEditingNickname.value = false;
};

const saveNickname = async () => {
  if (!editableNickname.value.trim()) {
    alert('昵称不能为空！');
    return;
  }
  if (editableNickname.value === originalNickname) {
    isEditingNickname.value = false;
    return;
  }

  isUpdatingNickname.value = true;
  try {
    const response = await apiClient.put('/users/me', {
      nickname: editableNickname.value,
    });
    if (response.data.success) {
      authStore.user = response.data.data;
      isEditingNickname.value = false;
    } else {
      alert('更新失败: ' + response.data.message);
      cancelEditNickname();
    }
  } catch (error) {
    alert('更新失败: ' + (error.response?.data?.message || error.message));
    cancelEditNickname();
  } finally {
    isUpdatingNickname.value = false;
  }
};

const changePassword = async () => {
  if (
    passwordForm.value.newPassword !== passwordForm.value.confirmNewPassword
  ) {
    alert('两次输入的新密码不一致！');
    return;
  }
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    alert('密码不能为空！');
    return;
  }

  isChangingPassword.value = true;
  try {
    await apiClient.put('/users/me/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    });
    alert('密码修改成功！下次登录请使用新密码。');
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmNewPassword: '',
    };
  } catch (error) {
    alert(
      '密码修改失败: ' +
        (error.response?.data?.message || '请检查当前密码是否正确。')
    );
  } finally {
    isChangingPassword.value = false;
  }
};
</script>

<style scoped>
.settings-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}
.page-title {
  font-size: 2.2rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 2.5rem;
}
.settings-section {
  margin-bottom: 3rem;
}
.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e0e0e0;
}
.profile-card,
.form-card {
  background-color: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}
.profile-card {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}
.avatar-display {
  flex-shrink: 0;
}
.current-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.info-fields {
  width: 100%;
}
.field {
  margin-bottom: 1rem;
}
.field label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}
.field input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  background-color: #f8f9fa;
  font-size: 1rem;
  cursor: not-allowed;
}
.field input[type='password'] {
  cursor: text;
  background-color: #fff;
}
.avatar-upload-area {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 1rem 0;
}
.file-name {
  font-style: italic;
  color: #555;
}

.button-primary,
.button-outline {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}
.button-primary {
  background-color: var(--color-theme-green);
  color: white;
}
.button-primary:disabled {
  background-color: #9e9e9e;
  cursor: not-allowed;
}
.button-outline {
  background-color: transparent;
  color: var(--color-theme-green);
  border: 1px solid var(--color-theme-green);
}

.form-card button.button-primary {
  margin-top: 1rem;
  float: right;
}

.editable-field {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.editable-field input {
  flex-grow: 1;
  background-color: #fff;
  cursor: text;
}

.editable-field input[readonly] {
  background-color: #f8f9fa;
  cursor: not-allowed;
  color: #333; 
}

.editable-field input.is-editing {
  border-color: var(--color-theme-green);
  box-shadow: 0 0 0 2px rgba(45, 181, 143, 0.2);
}

.edit-actions {
  display: flex;
  gap: 0.5rem;
}

.button-edit,
.button-save,
.button-cancel {
  padding: 0.6rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid;
  background-color: white;
  font-weight: 600;
  transition: all 0.2s;
  white-space: nowrap;
}

.button-edit {
  color: var(--color-theme-green);
  border-color: var(--color-theme-green);
}

.button-edit:hover {
  background-color: var(--color-theme-green);
  color: white;
}

.button-save {
  color: #4caf50; /* Green */
  border-color: #4caf50;
}
.button-save:hover {
  background-color: #4caf50;
  color: white;
}
.button-save:disabled {
  border-color: #ccc;
  color: #999;
  background-color: #f8f8f8;
  cursor: not-allowed;
}

.button-cancel {
  color: #757575; /* Gray */
  border-color: #ccc;
}
.button-cancel:hover {
  background-color: #e0e0e0;
  border-color: #adadad;
}
</style>
