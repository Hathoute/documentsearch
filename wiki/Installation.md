# Installation

## Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.hathoute.lib</groupId>
    <artifactId>documentsearch</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### SNAPSHOT builds

The current version is `1.0-SNAPSHOT`. To use SNAPSHOT builds, you may need to add the Sonatype OSSRH snapshot repository (or your own artifact repository if deploying internally):

```xml
<repositories>
    <repository>
        <id>ossrh-snapshots</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

### Released versions (GitHub Packages)

When a release tag (`v*`) is pushed, the library is automatically deployed to GitHub Packages via the [[ci.yml|CI pipeline]]. To consume releases from GitHub Packages, configure the repository in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/<owner>/documentsearch</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

You will also need to authenticate with a GitHub Personal Access Token that has `packages:read` scope. Add your token to `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username><your-github-username></username>
            <password><your-github-personal-access-token></password>
        </server>
    </servers>
</settings>
```

## Gradle

```groovy
implementation 'com.hathoute.lib:documentsearch:1.0-SNAPSHOT'
```

## Dependencies included

open-elastic bundles the following clients — no additional dependencies are required to use either backend:

| Dependency | Version |
|---|---|
| `co.elastic.clients:elasticsearch-java` | 9.3.0 |
| `org.opensearch.client:opensearch-rest-client` | 3.7.0 |
| `org.opensearch.client:opensearch-java` | 3.9.0 |

If you need to override any of these versions, see [[Version Overrides]].
