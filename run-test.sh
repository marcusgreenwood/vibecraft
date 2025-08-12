#!/bin/bash
set -e
echo "🧪 Starting automated Vibecraft mod test..."
./gradlew build
echo "🚀 Running automated test..."
mkdir -p run
# Prevent the test runner JVM from taking foreground focus on macOS while keeping AWT enabled for Robot
java -Dapple.awt.UIElement=true -Djava.awt.headless=false -cp build/libs/vibecraft-1.0.0.jar:build/classes/java/test:build/resources/test:deps/* com.vibecraft.automated.VibecraftTestRunner
echo "📊 Checking final test results..."
if [ -f "test-result.txt" ] && [ "$(cat "test-result.txt")" = "PASS" ]; then
    echo "🎉 ✅ All tests passed!"
    exit 0
else
    echo "💥 ❌ Test failed!"
    exit 1
fi