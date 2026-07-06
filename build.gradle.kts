plugins {
    base
}

tasks.register("doctor") {
    group = "verification"
    description = "Checks the local Apex project structure."

    doLast {
        val root = project.rootDir

        fun exists(path: String) = root.resolve(path).exists()
        fun printCheck(path: String) {
            val mark = if (exists(path)) "OK " else "MISS"
            println("$mark  $path")
        }

        println("Apex project doctor")
        println("===================")
        printCheck("backend/go.mod")
        printCheck("backend/cmd/api/main.go")
        printCheck("backend/migrations/00001_init.sql")
        printCheck("backend/migrations/00002_seed_dev.sql")
        printCheck("web-demo/index.html")
        printCheck("client/shared/src/commonMain")
        println()
        println("Backend:")
        println("  cd backend")
        println("  HTTP_ADDR=\":8082\" DATABASE_URL=\"postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable\" make run")
        println()
        println("Web demo:")
        println("  ./gradlew :webApp:wasmJsBrowserDevelopmentRun")
        println("  open http://127.0.0.1:5173")
    }
}

tasks.register<Exec>("backendTest") {
    group = "verification"
    description = "Runs Go backend tests."
    workingDir = file("backend")
    commandLine("go", "test", "./...")
}

tasks.register<Exec>("backendRun") {
    group = "application"
    description = "Runs backend API on port 8082."
    workingDir = file("backend")
    environment("HTTP_ADDR", ":8082")
    environment("DATABASE_URL", "postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable")
    commandLine("make", "run")
}

tasks.register<Exec>("webDemoRun") {
    group = "application"
    description = "Runs static Apex web demo on http://127.0.0.1:5173."
    commandLine("python3", "-m", "http.server", "5173", "--directory", "web-demo")
}
