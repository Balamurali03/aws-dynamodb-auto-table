<h1 align="center" style="font-size:42px;">
  🚀 dynamodb-auto-table
</h1>

<p align="center" style="font-size:18px; color:#555;">
  <b>Automatic DynamoDB table creation for Spring Boot using AWS Enhanced Client annotations</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/AWS-DynamoDB-orange" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen" />
  <img src="https://img.shields.io/badge/Java-17+-blue" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-lightgrey" />
</p>

<hr style="margin:30px 0;"/>

<h2>✨ What is this?</h2>

<p style="font-size:15px;">
  <b>dynamodb-auto-table</b> is a <b>Spring Boot auto-configuration library</b> that automatically creates
  DynamoDB tables <b>at application startup</b> by reading schema information directly from
  <b>AWS DynamoDB Enhanced Client annotations</b>.
</p>

<ul>
  <li>✅ Your entity is the <b>single source of truth</b></li>
  <li>✅ No manual table creation</li>
  <li>✅ No duplicate schema definitions</li>
</ul>

<hr/>

<h2>🎯 Why do we need this?</h2>

<p>
In real-world microservices, teams struggle with:
</p>

<ul>
  <li>❌ Manual DynamoDB table creation</li>
  <li>❌ Schema duplication across environments</li>
  <li>❌ Drift between local, QA, and AWS</li>
  <li>❌ Boilerplate infrastructure code</li>
</ul>

<p align="center" style="font-size:16px;">
  <b>🚫 All that stops here.</b>
</p>

<p>
<b>dynamodb-auto-table</b> removes this friction while staying
<b>100% AWS-aligned</b> and <b>Spring Boot–native</b>.
</p>

<hr/>

<h2>🧠 Design Principles</h2>

<ul>
  <li>✔ Zero credential handling</li>
  <li>✔ Zero application-specific configuration</li>
  <li>✔ Zero schema mutation</li>
  <li>✔ No destructive operations</li>
  <li>✔ Uses only AWS SDK v2 Enhanced Client metadata</li>
</ul>

<hr/>

<h2>⚙️ Features</h2>

<ul>
  <li>✔ Auto-creates DynamoDB tables</li>
  <li>✔ Reads schema from AWS annotations</li>
  <li>✔ Supports:
    <ul>
      <li>Partition Key</li>
      <li>Sort Key</li>
      <li>Global Secondary Indexes (GSI)</li>
    </ul>
  </li>
  <li>✔ Works with:
    <ul>
      <li>DynamoDB Local</li>
      <li>AWS DynamoDB</li>
      <li>IAM Roles (EC2 / ECS / EKS)</li>
    </ul>
  </li>
  <li>✔ Spring Boot AutoConfiguration</li>
  <li>✔ No <code>application.yml</code> or <code>application.properties</code></li>
</ul>

<hr/>

<h2>📦 Installation</h2>

```xml
<dependency>
  <groupId>io.github.balamurali03</groupId>
  <artifactId>dynamodb-auto-table</artifactId>
  <version>1.0.0</version>
</dependency>
```
<p><i>No additional configuration is required.</i></p>

<hr/>

<h2>🚀 Quick Start (5 Minutes)</h2>

<h3>1️⃣ Create a DynamoDB Entity</h3>

<pre style="background:#f6f8fa; padding:15px; border-radius:6px;">
@DynamoDbBean
@DynamoEntity(tableName = "users") OR @DynamoEntity
public class User {

    @DynamoDbPartitionKey
    private String userId;

    @DynamoDbSortKey
    private String createdAt;

    @DynamoDbSecondaryPartitionKey(indexNames = "email-index")
    private String email;

    // getters & setters
}

</pre>

<h3>2️⃣ Run Your Spring Boot Application</h3>

<ul>
  <li>✔ Entity is scanned</li>
  <li>✔ Schema is extracted</li>
  <li>✔ Table is created if missing</li>
</ul>

<hr/>

<h2>🔄 Complete Startup Flow</h2>

<ol>
  <li><b>Auto-Configuration</b> – Spring Boot loads DynamoAutoConfiguration</li>
  <li><b>Entity Scanning</b> – Finds all @DynamoEntity classes</li>
  <li><b>Schema Extraction</b> – Reads AWS annotations</li>
  <li><b>Table Creation</b> – Safe, non-destructive creation</li>
</ol>

<hr/>

<h2>📊 Supported & Unsupported Features</h2>

<h3>✅ Supported</h3>
<ul>
  <li>Partition Key</li>
  <li>Sort Key</li>
  <li>Global Secondary Index (GSI)</li>
  <li>On-Demand & Provisioned billing</li>
  <li>DynamoDB Local</li>
</ul>

<h3>❌ Not Supported (By Design)</h3>
<ul>
  <li>Table deletion</li>
  <li>Table updates</li>
  <li>TTL</li>
  <li>Streams</li>
</ul>

<hr/>

<h2>🔐 Security & Credentials</h2>

<p>
This library does <b>not</b> manage AWS credentials.
It relies entirely on the <b>AWS SDK Default Credential Provider Chain</b>.
</p>

<hr/>

<h2>📜 License</h2>

<pre>
Apache License 2.0
</pre>

<hr/>

<p align="center" style="font-size:16px;">
  <b>⭐ Star the repository if this saved you time ⭐</b>
</p>

<p align="center" style="color:#666;">
  © 2025 <b>Balamurali R</b> — Built with ❤️ for the AWS & Java community
</p>
