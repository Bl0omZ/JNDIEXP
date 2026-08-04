# 批次 0：卫生组实施计划

> **For agentic workers:** 按顺序逐步执行，每步完成后跑验证命令确认再进入下一步。

**Goal:** 合并重复工具类、删除演示 main、尝试 fat jar 瘦身，为后续扩链批次提供干净基线。

**Architecture:** 三个独立子任务，按依赖顺序执行：命名规范化 → 删演示 main → 瘦身。

**Tech Stack:** Java 8 / Maven / maven-assembly-plugin（现有）→ maven-shade-plugin（替换）

## Global Constraints

- JDK 8 编译目标
- `mvn test` 52 用例基线只增不减
- dry-run 11 路由全过
- `chain-golden.txt` 旧行不变
- 每个子任务独立 commit

---

### Task 1: 合并 Util → Functions（消除双工具类）

**分析结论：**

两个类的职责重叠（`getClassBytes`/`getRandomString` 重复实现）。`Functions` 是主用（Cache/CommandTemplate/RouteParser 依赖），`Util` 是次用（CommandTemplate/TomcatByPass 依赖）。方案：把 `Util` 独有方法迁入 `Functions`，删除 `Util.java`，全仓库 import 跟进。

`Util` 独有方法（需迁移到 Functions）：
- `getClassCode(Class)` — 被 `CommandTemplate:169`、`TomcatByPass:81` 调用
- `base64Encode(byte[])` — 被 `TomcatByPass:41` 调用、`getClassCode` 内部调用
- `serialize(Object)` — 无外部调用，但保留备用
- `getCmdFromBase(String)` — 无外部调用，但保留备用
- `getIPAndPortFromBase(String)` — 无外部调用，但保留备用

`Util` 中 `getRandomString` 和 `getClassBytes` 与 `Functions` 重复，迁移时丢弃。

注意：`ysoserial/exploit/JenkinsListener.java:193` 的 `Util.computeMethodHash` 是 `sun.rmi.server.Util`，不是 bloom 的 Util，不受影响。

**Files:**
- Modify: `src/main/java/com/bloom/util/Functions.java` — 追加 Util 独有方法
- Delete: `src/main/java/com/bloom/util/Util.java`
- Modify: `src/main/java/com/bloom/Template/CommandTemplate.java` — import 改 Functions
- Modify: `src/main/java/com/bloom/payload/TomcatByPass.java` — import 改 Functions

**Steps:**

- [ ] **Step 1: 在 Functions.java 末尾追加 Util 独有方法**

把 `base64Encode`、`getClassCode`、`serialize`、`getCmdFromBase`、`getIPAndPortFromBase` 五个方法从 Util.java 原样复制到 Functions.java class body 末尾。`getClassCode` 内部调用改为 `Functions.getClassBytes` 和 `Functions.base64Encode`。

- [ ] **Step 2: 修改 CommandTemplate.java import 和调用**

`import com.bloom.util.Util;` → 删除（Functions 已有 import）。
第 169 行 `Util.getClassCode(clazz)` → `Functions.getClassCode(clazz)`。

- [ ] **Step 3: 修改 TomcatByPass.java import 和调用**

`import com.bloom.util.Util;` → 删除或改为 `import com.bloom.util.Functions;`。
第 41 行 `Util.base64Encode(bytes)` → `Functions.base64Encode(bytes)`。
第 81 行 `Util.getClassCode(clazz)` → `Functions.getClassCode(clazz)`。

- [ ] **Step 4: 删除 Util.java**

```bash
rm src/main/java/com/bloom/util/Util.java
```

- [ ] **Step 5: 编译验证**

```bash
mvn compile -q && echo "OK"
```
Expected: OK，零错误。

- [ ] **Step 6: 全量测试**

```bash
mvn test -q 2>&1 | tail -3
```
Expected: Tests run: 52, Failures: 0, Errors: 0, Skipped: 0

- [ ] **Step 7: dry-run 验证**

```bash
mvn exec:java -Dexec.mainClass="com.bloom.server.LdapServer" -Dexec.args="--dry-run" -q 2>&1 | grep -c "\[OK\]"
```
Expected: 11

- [ ] **Step 8: Commit**

```bash
git add -A && git commit -m "refactor: 合并 Util 到 Functions，消除双工具类"
```

---

### Task 2: 删除 payload.java 演示 main

**Files:**
- Modify: `src/main/java/com/bloom/exploit/payload.java` — 删除 main 方法

**Steps:**

- [ ] **Step 1: 删除 main 方法（第 30-34 行）**

保留 `getPayload` 静态方法（被 chain 系统使用），只删 main。

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q && echo "OK"
```

- [ ] **Step 3: 全量测试**

```bash
mvn test -q 2>&1 | tail -3
```
Expected: Tests run: 52

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/bloom/exploit/payload.java && git commit -m "chore: 删除 payload.java 演示 main（P3 备忘）"
```

---

### Task 3: fat jar 瘦身（assembly → shade + minimizeJar）

**分析结论：**

当前 60MB jar 用 `maven-assembly-plugin` 的 `jar-with-dependencies`，把所有依赖完整打入。所有 46 个依赖都有代码引用（大多是 ysoserial gadget 链的编译依赖），不能整个剔除。但多数依赖中大量 class 从未被加载——`maven-shade-plugin` 的 `minimizeJar` 可通过字节码分析自动剔除未引用 class，预计减 30-50%。

注意：ysoserial 链通过反射/反序列化触发目标类，`minimizeJar` 可能误删。需配置 `<filter>` 保留关键包。

**Files:**
- Modify: `pom.xml` — assembly 插件替换为 shade 插件 + minimizeJar + 必要 filter

**Steps:**

- [ ] **Step 1: 替换 pom.xml 中的 assembly 插件为 shade 插件**

删除 `maven-assembly-plugin` 配置块，加入：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
                <minimizeJar>true</minimizeJar>
                <filters>
                    <!-- ysoserial gadget 链通过反射触发，不可被 minimize 剔除 -->
                    <filter>
                        <artifact>*:*</artifact>
                        <includes>
                            <include>ysoserial/**</include>
                            <include>com/bloom/**</include>
                        </includes>
                    </filter>
                    <!-- gadget 依赖保留完整包（反序列化链通过类名触发） -->
                    <filter>
                        <artifact>commons-collections:commons-collections</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.apache.commons:commons-collections4</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>commons-beanutils:commons-beanutils</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.codehaus.groovy:groovy</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.springframework:*</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.hibernate:hibernate-core</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>com.vaadin:*</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.python:jython-standalone</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.clojure:clojure</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.mozilla:rhino</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>xalan:xalan</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>rome:rome</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.apache.myfaces.core:myfaces-impl</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.apache.wicket:wicket-util</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>com.mchange:c3p0</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.beanshell:bsh</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>commons-fileupload:commons-fileupload</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>org.javassist:javassist</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                    <filter>
                        <artifact>hudson:remoting</artifact>
                        <includes><include>**</include></includes>
                    </filter>
                </filters>
                <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>com.bloom.server.LdapServer</mainClass>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 2: 构建并比较体积**

```bash
mvn package -q -DskipTests && ls -lh target/JNDIInject-*.jar
```

记录新体积，与旧 60MB 对比。

- [ ] **Step 3: 全量测试**

```bash
mvn test -q 2>&1 | tail -3
```
Expected: Tests run: 52

- [ ] **Step 4: dry-run 验证**

```bash
java -jar target/JNDIInject-1.3-SNAPSHOT.jar --dry-run 2>&1 | grep -c "\[OK\]"
```
Expected: 11（用打包后的 jar 验证，确保 minimize 没误删关键类）

- [ ] **Step 5: 如果 Step 4 失败，追加 filter 保留缺失包并重复 Step 2-4**

排查 `ClassNotFoundException`/`NoClassDefFoundError`，对应依赖加 `<include>**</include>` filter。

- [ ] **Step 6: 记录瘦身结果**

在 `docs/优化方案总览.md` 的阶段 4 瘦身项追加实际结果（旧体积/新体积/保留的 filter 列表）。

- [ ] **Step 7: Commit**

```bash
git add pom.xml docs/优化方案总览.md && git commit -m "build: assembly→shade+minimizeJar，fat jar 从 60MB 降至 XMB"
```

---

## 风险记录

| 风险 | 缓解 |
|---|---|
| `minimizeJar` 误删反序列化链触发的类 | 所有 gadget 库全量保留（filter include **） |
| hutool `FileReader` 被 minimize 剔除 | hutool 是 HttpServerStart 的编译依赖，shade 静态分析可追踪；如失败单独加 filter |
| Util 合并遗漏调用点 | Step 5 编译即暴露；grep 确认无遗漏 |

## 预期产出

- 消除 `Util.java`，工具方法单一入口 `Functions`
- 删除弹计算器演示 main
- jar 体积从 60MB 降至预估 35-45MB（取决于 jython/hibernate 内部未引用 class 占比）
- 52 用例 + 11 路由 dry-run 基线不变
