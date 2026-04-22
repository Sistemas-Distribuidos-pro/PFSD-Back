FROM public.ecr.aws/amazoncorretto/amazoncorretto:21-al2023-jdk
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
