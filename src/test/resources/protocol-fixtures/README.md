# AutoJs6 protocol replay fixtures

这些 fixtures 是 `complete-autojs6-vscode-parity` 的协议基线，不代表项目同步已经实现。

覆盖范围：

- JSON frame: `hello-device-to-ide.json`、`hello-ide-to-device.json`、`command-run-file.json`、`log-event.json`
- bytes frame: `bytes-frame.sample.json`
- `bytes_command`: `bytes-command-run-project.json`、`bytes-command-save-project.json`
- 设备反向 command: `device-reverse-command-rerun.json`

来源约束：

- frame header 为 8 字节：前 4 字节 big-endian payload length，后 4 字节 big-endian type。
- JSON type 为 `1`，bytes type 为 `2`。
- 项目同步顺序必须是先发送 bytes payload，再发送 JSON `type=bytes_command`。
- fixtures 中的 bytes payload 只是 frame/ordering 样本，不得作为 fake project sync success 使用；真正 project sync 仍需 zip/md5/device replay 验证。
