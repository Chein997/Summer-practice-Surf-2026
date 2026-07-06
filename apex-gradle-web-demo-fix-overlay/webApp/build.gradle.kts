plugins {
    base
}

val webDemoDir = rootProject.layout.projectDirectory.dir("web-demo").asFile

tasks.register<Exec>("wasmJsBrowserDevelopmentRun") {
    group = "application"
    description = "Compatibility task: runs Apex web-demo on http://127.0.0.1:5173."

    doFirst {
        if (!webDemoDir.resolve("index.html").exists()) {
            throw GradleException(
                "web-demo/index.html not found. Apply apex-web-demo-overlay first."
            )
        }

        println("Starting Apex web demo")
        println("URL: http://127.0.0.1:5173")
        println("API base on the page should be: http://127.0.0.1:8082")
        println()
    }

    commandLine("python3", "-m", "http.server", "5173", "--directory", webDemoDir.absolutePath)
}

tasks.register<Exec>("jsBrowserDevelopmentRun") {
    group = "application"
    description = "Alias for wasmJsBrowserDevelopmentRun; runs Apex web-demo."
    commandLine("python3", "-m", "http.server", "5173", "--directory", webDemoDir.absolutePath)
}

tasks.register("checkWebDemo") {
    group = "verification"
    description = "Checks that web-demo exists."

    doLast {
        val index = webDemoDir.resolve("index.html")
        if (!index.exists()) {
            throw GradleException("Missing web-demo/index.html")
        }
        println("OK: ${index.absolutePath}")
    }
}
