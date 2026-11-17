# 观澜知识研习平台

观澜知识研习平台是一个面向学习与知识分享的前后端分离项目。用户可以浏览题目、编写 Markdown 笔记，通过搜索查找内容，并使用点赞、收藏和评论等功能。项目围绕用户认证、内容管理和数据统计等场景，提供了 Java 后端开发与软件测试实践。

## 功能介绍

- 用户注册与登录：使用 BCrypt 处理密码，通过 JWT 识别登录用户；支持邮箱验证码。
- 题目与笔记：按分类浏览题目、编写笔记；支持 Markdown 题目批量导入。
- 内容互动：支持笔记点赞、收藏与评论，登录用户可以管理自己的笔记。
- 内容检索：结合 Jieba 分词、MySQL 全文检索和 Redis 搜索结果缓存。
- 数据统计：提供笔记提交热力图、排行榜及定时统计功能。

## 技术栈

| 方向 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 2.7、Spring MVC、MyBatis、Spring Security |
| 数据与缓存 | MySQL、Redis |
| 认证与邮件 | JWT、BCrypt、Spring Mail |
| 文本处理 | Flexmark、Jieba |
| 前端 | React、TypeScript、Vite、Ant Design |

## 项目结构

```text
.
├── backend/             # Spring Boot 后端
│   ├── src/main/java/   # 接口、业务逻辑及工具类
│   └── src/main/resources/
│       └── mapper/      # MyBatis SQL 映射
├── frontend/            # React 前端
├── LICENSE
└── README.md
```

## 本地运行

运行前需要准备 Java 17、Node.js（项目 `.nvmrc` 指定 20.10.0）、MySQL 和 Redis。邮件功能需要可用的 SMTP 配置。

1. 创建数据库并准备项目所需表结构。本仓库当前未附带数据库初始化 SQL，需要根据实际数据库结构准备。
2. 检查 `backend/src/main/resources/application.yaml` 中的 Redis、邮件和 JWT 配置。仓库中的敏感配置已替换为占位符，请按本地环境填写。
3. 当前配置启用 `dev` 环境。参考实际环境在 `backend/src/main/resources/` 下创建 `application-dev.yaml`，配置数据源及本地文件存储路径。
4. 在 `backend` 目录启动后端：

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. 在 `frontend` 目录安装依赖并启动前端：

   ```powershell
   npm install
   npm run dev
   ```

后端默认端口为 8080。前端实际访问地址以 Vite 启动输出及环境配置为准。

## 设计与测试关注点

- 验证码通过 Redis 队列交给定时任务发送；可测试发送频率、过期和并发请求。
- 批量导入使用 Flexmark 解析标题和列表，通过事务处理符合回滚条件的导入异常。
- 笔记搜索按关键词和分页缓存结果；修改笔记后可验证缓存数据是否及时更新。
- 涉及笔记修改和删除的接口需要验证登录身份，并检查当前用户是否为笔记作者。

