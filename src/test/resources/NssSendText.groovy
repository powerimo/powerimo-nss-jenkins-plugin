node {
    def result = nssSendText(dryRun: true, text: "123", caption: "my caption")
    echo "Result is ${result}"
}