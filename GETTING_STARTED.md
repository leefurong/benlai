# 快速开始

这是一个 Benlai 的概念验证（Proof of Concept）实现。

## 运行步骤

1. **安装依赖**（如果还没有安装 Clojure CLI）：
   ```bash
   # 确保已安装 Clojure CLI (clj)
   ```

2. **启动服务器**（选择其中一个应用）：
   
   **计数器应用**：
   ```bash
   clj -M -m my-app.core
   ```
   
   **Todo List 应用**：
   ```bash
   clj -M -m my-app.todo
   ```

3. **打开浏览器**：
   访问 http://localhost:8080

4. **测试功能**：
   
   **计数器应用**：
   - 点击 "+" 按钮增加计数
   - 点击 "-" 按钮减少计数
   - 观察服务器端状态的变化如何自动反映到 UI
   
   **Todo List 应用**：
   - 在输入框中输入待办事项，点击 "Add" 添加
   - 点击复选框标记为完成/未完成
   - 点击 "Delete" 删除单个待办事项
   - 点击 "Mark All Complete/Incomplete" 批量操作
   - 点击 "Clear X completed" 清除所有已完成项

## 工作原理

1. **状态管理**：所有状态存储在服务器端的 `atom` 中
2. **事件处理**：点击按钮时，客户端发送事件到服务器
3. **视图渲染**：服务器使用 Hiccup 渲染视图
4. **DOM 更新**：服务器计算差异并发送补丁，客户端应用更新

## 项目结构

```
benlai/
├── src/
│   ├── benlai/
│   │   └── core.clj          # 核心库（defview, start-server!）
│   └── my_app/
│       ├── core.clj          # 示例应用（计数器）
│       └── todo.clj          # Todo List 应用
├── resources/
│   └── public/
│       └── index.html        # 客户端 HTML/JS
├── deps.edn                  # 项目依赖
└── README.md
```

## 下一步

这个 POC 实现了最基本的功能：
- ✅ 服务器端状态管理
- ✅ 事件处理
- ✅ Hiccup 视图渲染
- ✅ 简单的 DOM 更新

未来可以改进：
- 更高效的树差异算法
- WebSocket 支持（实时更新）
- 更智能的 DOM 补丁
- 多会话支持

