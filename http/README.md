## HTTP Request files
### 1. How to use it?
Docs -> https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html

### 2. Run-all script

#### 2.1 About
The script use docker image of Intellij HTTP Client. It looks for all `*.http` files and send it to docker image.
When it is done, the report file should be generated in location `./_report/report.xml`.
The report file is in JUnit format, and it can be imported and viewed in IntelliJ

#### 2.2 How to use
If you would like to execute all http files at once, you can use `run_all.sh` script.
Pattern of use:
```bash
./run_all.sh <environment_name>
```

environment_name e.g `localhost`, `PROD` (or any other you had defines in http file)
e.g.
```bash
./run_all.sh localhost
```
