# 大肥鱼桌宠 · 安卓版

把 Electron 版 `body-of-fish` 重做为安卓原生版：悬浮窗桌宠 + 拖拽 + DeepSeek 对话。
**不需要本地装 Android Studio**，Push 到 GitHub 就自动出 APK。

## 一、上传到 GitHub

1. 手机上打开 https://github.com/new，新建仓库，例如 `fish-pet-android`，**勾选 Public**，不要勾 Add README。
2. 拿到本工程压缩包解压后，用以下任一方式上传：
   - **网页上传**：仓库页 → `Add file` → `Upload files` → 把解压出来的**文件夹里的内容**拖进去
     （注意：要保证根目录直接能看到 `settings.gradle`、`build.gradle`、`app/`、`.github/`，不要多套一层文件夹）。
     网页拖拽可能漏掉以 `.` 开头的 `.github` 和 `.gitignore`，上传后务必确认 `.github/workflows/build.yml` 存在。
   - **MT 管理器 / Termux（推荐，最稳）**：见文末命令。

## 二、编译出 APK

1. 打开仓库顶部 `Actions` 标签，左侧选 `Build APK`。
2. 如果没自动跑，点 `Run workflow` → `Run workflow`。
3. 等 2~4 分钟，绿色 ✅ 后点进这次运行，页面底部 `Artifacts` 下载 **fish-pet-debug-apk**（zip 里就是 apk）。
4. 手机上解压安装即可（需允许“安装未知来源应用”）。

## 三、首次使用

1. 打开 App → 点「授予悬浮窗权限」→ 在系统设置里允许「显示在其他应用上层」。
2. 填 DeepSeek API Key（在 platform.deepseek.com 申请）→「保存 Key」。
3. 点「启动大肥鱼」，屏幕上出现小鱼。
4. **点一下**鱼 = 打开聊天窗；**长按拖动** = 换位置。
5. 通知栏有常驻通知，点「关闭」可退出。

## 四、文件结构
