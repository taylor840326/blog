## Spring Data JPA 向导 (Spring Data JPA Tutorial)
-----

小白零基础学习Spring Data，没有一个系统的知识框架学起来很费劲。

总是感觉有无数的坑需要填，今天找到一篇外国大神文章感觉不错，基于这个文章整理我的Spring Data JPA知识体系.

文章的地址为：
```html
https://www.petrikainulainen.net/spring-data-jpa-tutorial/
```

### 1. Introducing: Spring Data JPA Tutorial

This tutorial describes how you can create JPA repositories without writing any boilerplate code, and it consists of the following blog posts

```text
注：boilerplate code，泛指那些冗余但是又不得不写的代码。
```

[Spring Data JPA Tutorial: Introduction](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-introduction/)
provides a quick introduction to Spring Data JPA. It describes what Spring Data JPA really is and provides an overview of the Spring Data repository interfaces.

[Spring Data JPA Tutorial: Getting the Required Dependencies](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-getting-the-required-dependencies/)
describes how you can get the required dependencies.

[Spring Data JPA Tutorial: Configuration](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-part-one-configuration/) 
helps you to configure the persistence layer of a Spring application that uses Spring Data JPA and Hibernate.

[Spring Data JPA Tutorial: CRUD](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-part-two-crud/)
describes how you can create a Spring Data JPA repository that provides CRUD operations for an entity.

[Spring Data JPA Tutorial: Introduction to Query Methods](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-introduction-to-query-methods/)
gives a very short introduction to query methods. It also describes what kind of values you can return from your query methods and how you can pass method parameters to your query methods.

[Spring Data JPA Tutorial: Creating Database Queries From Method Names](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-creating-database-queries-from-method-names/) 
describes how you can create database queries from the method names of your query methods.

[Spring Data JPA Tutorial: Creating Database Queries With the @Query Annotation](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-creating-database-queries-with-the-query-annotation/) 
describes how you can create database queries by annotating your query methods with the @Query annotation.

[Spring Data JPA Tutorial: Creating Database Queries With Named Queries](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-creating-database-queries-with-named-queries/)
describes how you can create database queries by using named queries.

[Spring Data JPA Tutorial: Creating Database Queries With the JPA Criteria API](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-part-four-jpa-criteria-queries/)
describes how you can create dynamic queries by using the JPA Criteria API.

[Spring Data JPA Tutorial: Creating Database Queries With Querydsl](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-part-five-querydsl/)
describes how you can create dynamic database queries by using Querydsl.

[Spring Data JPA Tutorial: Sorting](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-part-six-sorting/) 
describes how you can sort your query results.

[Spring Data JPA Tutorial: Pagination](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-part-seven-pagination/) 
helps you to paginate your query results.

[Spring Data JPA Tutorial: Auditing, Part One](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-auditing-part-one/) 
describes how you can add the creation and modification time fields into your entities by using the auditing infrastructure of Spring Data JPA.

[Spring Data JPA Tutorial: Auditing, Part Two](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-auditing-part-two/) 
describes how you can add the information of the authenticated user, who created and/or updated an entity, into your entities by using the auditing infrastructure of Spring Data JPA.

[Spring Data JPA Tutorial: Adding Custom Methods to a Single Repository](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-part-eight-adding-functionality-to-a-repository/) 
describes how you can add custom methods to a single repository.

[Spring Data JPA Tutorial: Adding Custom Methods to All Repositories](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-adding-custom-methods-into-all-repositories/) 
describes how you can add custom methods to all repositories.

[Spring Data JPA Tutorial: Integration Testing](https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-integration-testing/) 
describes how you can write integration tests for your Spring Data JPA repositories.

### 2. Other Resources
This section showcases useful material created by other developers. If you have written a blog post or recorded a video about Spring Data JPA, and want to include it to this section, ping me on Twitter and I will check it out. If I think that it is useful, I will add it to this section.

### 2.1. Java Persistence API

Reference Manuals and Official Guides

[The Java EE 6 Tutorial Part VI: Persistence](http://docs.oracle.com/javaee/6/tutorial/doc/bnbpy.html)

[Querydsl Reference Manual](http://www.querydsl.com/static/querydsl/3.3.2/reference/html/)

[Querydsl API](http://www.querydsl.com/static/querydsl/3.3.2/apidocs/)


### 2.2. Blog Posts

[Dynamic, typesafe queries in JPA 2.0](http://www.ibm.com/developerworks/java/library/j-typesafejpa/)

[JPA Criteria API by samples – Part-I](http://www.altuure.com/2010/09/23/jpa-criteria-api-by-samples-part-i/)

[JPA Criteria API by samples – Part-II](https://web.archive.org/web/20170829042703/http://www.altuure.com:80/2010/09/23/jpa-criteria-api-by-samples-%E2%80%93-part-ii)

[JPA 2 Criteria API Tutorial](http://www.jumpingbean.co.za/blogs/jpa2-criteria-api)

[JPA Pagination](http://www.baeldung.com/jpa-pagination)


### 2.3. Videos

[JPA: Embedding Embeddables within Entities](http://www.youtube.com/watch?v=CV-qLwYKajI)

[JPA: One to One Uni/Bi Directional Relationship Tutorial](http://www.youtube.com/watch?v=QNYJBe2AZ-I)

[JPA: One to Many Relationship](http://www.youtube.com/watch?v=j1xxxrpbwME)

[JPA: Mapping a Many To Many Relationship](http://www.youtube.com/watch?v=BO-Gy4XC6QE)

[JPA: Self Referencing Relationships](http://www.youtube.com/watch?v=GV2tA3_uKBE)

[JPA: Inheritance with @MappedSuperclass](http://www.youtube.com/watch?v=7kjiNuqsSq8)

[Inheritance with @Inheritance](http://www.youtube.com/watch?v=yvQrc2WigMc)

[JPA: @Inheritance with Table Per Class](http://www.youtube.com/watch?v=fek69xLYJ3Y)

[JPQL: The Basics of the Java Persistence Query Language](http://www.youtube.com/watch?v=KdJ4W7nqhVg)

[JPA / JPQL: Intermediate Queries with @NamedQuery](http://www.youtube.com/watch?v=qlpeBFJWTys)

[Criteria API Introduction](http://www.youtube.com/watch?v=J-f4jvljpgQ)


### 2.4. Spring Data JPA

Reference Manuals and Official Guides

[Spring Data JPA Reference Manual](http://docs.spring.io/spring-data/jpa/docs/1.5.x/reference/html/)

[Spring Data JPA API](http://docs.spring.io/spring-data/jpa/docs/1.5.x/api/)

[Spring Data REST Reference Manual](http://docs.spring.io/spring-data/rest/docs/2.0.x/reference/html/)

[Spring Data REST API](http://docs.spring.io/spring-data/rest/docs/2.0.x/api/)

[Getting started with Spring Data JPA](http://spring.io/blog/2011/02/10/getting-started-with-spring-data-jpa)

[Advanced Spring Data JPA – Specifications and Querydsl](http://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/)

[Accessing Data with JPA](http://spring.io/guides/gs/accessing-data-jpa/)

[Accessing JPA Data with REST](https://spring.io/guides/gs/accessing-data-rest/)


### 2.5. Blog Posts

[Distributed transactions with multiple databases, Spring Boot, Spring Data JPA and Atomikos](http://fabiomaffioletti.me/blog/2014/04/15/distributed-transactions-multiple-databases-spring-boot-spring-data-jpa-atomikos/)

[Spring Data repositories with multiple databases](http://scattercode.co.uk/2013/11/18/spring-data-multiple-databases/)

[The Persistence Layer with Spring Data JPA](http://www.baeldung.com/2011/12/22/the-persistence-layer-with-spring-data-jpa/)

[Spring JPA Data + Hibernate + MySQL + MAVEN](http://fruzenshtein.com/spring-jpa-data-hibernate-mysql/)

[Spring Data JPA with QueryDSL: Repositories made easy](https://blog.42.nl/articles/spring-data-jpa-with-querydsl-repositories-made-easy/)

[Spring Data REST in Action](http://www.javacodegeeks.com/2013/08/spring-data-rest-in-action.html)

[Spring MVC 3.2 with Spring Data REST (part 1)](http://krams915.blogspot.fi/2012/11/spring-mvc-32-with-spring-data-rest.html)

[Spring MVC 3.2 with Spring Data REST (part 2)](http://krams915.blogspot.fi/2012/11/spring-mvc-32-with-spring-data-rest_20.html)

[Spring boot and spring data jpa tutorial (part 1 / 2)](http://ufasoli.blogspot.ch/2014/02/spring-boot-and-spring-data-jpa.html)

[Spring boot and spring data jpa tutorial (part 2 / 2)](http://ufasoli.blogspot.fi/2014/02/spring-boot-and-spring-data-jpa_6.html)

### 2.6. Videos

[An Introduction to Spring Data](http://www.youtube.com/watch?v=jIae_pcG-9M)

[Spring Data Repositories – A Deep Dive](http://www.youtube.com/watch?v=JobDILuItcU)

[An Introduction to Spring Data JPA](http://www.youtube.com/watch?v=Yg2gCpBCkZw)

[Spring Data JPA Configuration Tutorial](http://www.youtube.com/watch?v=kM7Gr3XTzIg)

[Configuring Spring Data with a MySQL Database](http://www.youtube.com/watch?v=8B-mEllEuWE)

[Spring Data JPA: Defining Query Methods on Repositories](http://www.youtube.com/watch?v=SKRtcK7Fp3I)

[Spring Data REST: Easily export JPA entities directly to the web](http://www.youtube.com/watch?v=kaiH1HsacPs)

[Spring Data Repositories – Best Practices](http://www.youtube.com/watch?v=hwNyzkWENE0)

[Integration Testing of Spring Data JPA Repositories](http://www.youtube.com/watch?v=TItcLbGTRK0)
