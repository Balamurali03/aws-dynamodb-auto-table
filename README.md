<h1 align="center">🚀 dynamodb-auto-table</h1>

<p align="center">
  <b>Automatic DynamoDB table creation for Spring Boot using AWS Enhanced Client annotations</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/AWS-DynamoDB-orange" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen" />
  <img src="https://img.shields.io/badge/Java-17+-blue" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-lightgrey" />
</p>

---

## ✨ What is this?

**`dynamodb-auto-table`** is a **Spring Boot auto-configuration library** that automatically creates DynamoDB tables **at application startup** by reading schema information directly from **AWS Enhanced Client annotations**.

> ✅ Your entity becomes the **single source of truth**  
> ✅ No manual table creation  
> ✅ No duplicate schema definitions  

---

## 🎯 Why do we need this?

In real-world microservices, teams struggle with:

- Manual DynamoDB table creation
- Schema duplication across environments
- Drift between local, QA, and AWS
- Boilerplate infrastructure code

<p align="center"><b>❌ All that stops here.</b></p>

`dynamodb-auto-table` removes this friction while staying **100% AWS-aligned**.

---

## 🧠 Design Principles (Very Important)

This library is built with **strict AWS-grade principles**:

<ul>
  <li>✔ Zero credential handling</li>
  <li>✔ Zero application-specific configuration</li>
  <li>✔ Zero schema mutation</li>
  <li>✔ No destructive operations</li>
  <li>✔ Uses only AWS SDK v2 Enhanced Client metadata</li>
</ul>

---

## ⚙️ Features

✔ Auto-creates DynamoDB tables  
✔ Reads schema from AWS annotations  
✔ Supports:
<ul>
  <li>Partition Key</li>
  <li>Sort Key</li>
  <li>Global Secondary Indexes (GSI)</li>
</ul>

✔ Works with:
<ul>
  <li>DynamoDB Local</li>
  <li>AWS DynamoDB</li>
  <li>IAM Roles (EC2 / ECS / EKS)</li>
</ul>

✔ Spring Boot AutoConfiguration  
✔ No YAML / Properties required  

---

## 📦 Installation

Add the dependency:

```xml
<dependency>
  <groupId>com.framework</groupId>
  <artifactId>dynamodb-auto-table</artifactId>
  <version>1.0.0</version>
</dependency>

<p>No configuration required.</p> <hr/> <h2>🚀 Quick Start (5 Minutes)</h2> <h3>1️⃣ Create a DynamoDB Entity</h3>

@DynamoDbBean
@DynamoEntity
public class Sample {

    private String sampleId;
    private String ownerId;

    @DynamoDbPartitionKey
    public String getSampleId() {
        return sampleId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "owner-index")
    public String getOwnerId() {
        return ownerId;
    }
}
```

<h3>2️⃣ Run Your Spring Boot Application</h3> <p> That’s it. On startup: </p> <ul> <li>✔ Entity is scanned</li> <li>✔ Schema is extracted</li> <li>✔ Table is created if missing</li> </ul> <hr/> <h2>🔄 Complete Startup Flow</h2> <ol> <li><b>Auto-Configuration</b> – Spring Boot loads DynamoAutoConfiguration</li> <li><b>Entity Scanning</b> – Finds all @DynamoEntity classes</li> <li><b>Schema Extraction</b> – Reads AWS annotations</li> <li><b>Table Creation</b> – Creates tables safely</li> </ol> <hr/> <h2>📊 Supported & Unsupported Features</h2> <h3>✅ Supported</h3> <ul> <li>Partition Key</li> <li>Sort Key</li> <li>Global Secondary Index (GSI)</li> <li>On-Demand & Provisioned billing</li> <li>DynamoDB Local</li> </ul> <h3>❌ Not Supported (By Design)</h3> <ul> <li>Table deletion</li> <li>Table updates</li> <li>TTL</li> <li>Streams</li> </ul> <hr/> <h2>🔐 Security & Credentials</h2> <p> This library does <b>not</b> manage AWS credentials. It relies entirely on the AWS SDK default credential provider chain. </p> <hr/>

<hr/> <h2>📜 License</h2> <pre> Apache License 2.0 </pre> <hr/> <p align="center"> <b>⭐ Star the repository if this saved you time ⭐</b> </p>
