# Manual and Automated Software Deployment

This project compares two ways of deploying the same Java web application:

- **Manual deployment:** run Maven, copy the JAR, start the application, and check that it works.
- **Automated deployment:** run a Jenkins pipeline that tests, packages, deploys, and checks the application.

The application displays a simple web page. Maven builds the Java application, and JUnit tests it. The Jenkins pipeline is defined in `Jenkinsfile`.

## Requirements

- Linux with Bash, Git, curl, and a web browser
- Java JDK 17 or newer
- Maven 3.6 or newer
- Jenkins with the Pipeline, Git, and JUnit plugins
- Access to this Git repository from the Jenkins machine

Check the installed tools from the project directory:

```bash
java -version
mvn -version
git --version
curl --version
```

**Screenshot 1:** Capture the terminal showing these version commands.

## 1. Build and test the application

Open a terminal in the project folder and run:

```bash
mvn -B clean verify
```

Maven compiles the code, runs the test, and creates `target/deployment-demo-1.0.0.jar`. Check that the terminal shows `BUILD SUCCESS` and that the test has no failures.

**Screenshot 2:** Capture the test result and `BUILD SUCCESS` in the terminal.

## 2. Deploy manually

Make sure port 8081 is free. From the project folder, run:

```bash
mkdir -p "$HOME/devops-project3-manual"
cp target/deployment-demo-1.0.0.jar "$HOME/devops-project3-manual/app.jar"

PORT=8081 nohup java -jar "$HOME/devops-project3-manual/app.jar" > "$HOME/devops-project3-manual/app.log" 2>&1 &
echo $! > "$HOME/devops-project3-manual/app.pid"
```

Check that the application is responding:

```bash
curl --retry 10 --retry-connrefused --retry-delay 1 -fsS http://127.0.0.1:8081/health
```

The response should be `deployment-demo-ok`. Open <http://localhost:8081> in your browser.

**Screenshot 3:** Capture the application page in your browser.

After taking the screenshot, stop the manual application before running Jenkins on the same port. Run this immediately after the steps above, while the saved PID still belongs to the process you started:

```bash
kill "$(cat "$HOME/devops-project3-manual/app.pid")"
```

## 3. View the GitHub repository

Check your local changes and repository URL:

```bash
git status
git remote -v
```

Open the repository on GitHub. The repository should show `pom.xml`, `Jenkinsfile`, `src`, and `scripts`.

**Screenshot 4:** Capture the GitHub repository page showing these files.

To add this README to the existing `master` branch on GitHub, run:

```bash
git add README.md
git commit -m "Add project README"
git push origin master
```

## 4. Start Jenkins

If Jenkins is already installed as a Linux service, check it with:

```bash
sudo systemctl status jenkins --no-pager
```

If it is stopped, start it with:

```bash
sudo systemctl start jenkins
```

Open <http://localhost:8080> and sign in. If this is a new installation, complete the initial setup and install the required plugins.

If the service fails with `200/CHDIR` because `/var/lib/jenkins` is missing, the commands below create the missing home directory and start Jenkins. A new directory starts a **fresh Jenkins setup**. If you expected existing Jenkins jobs or settings, investigate the missing directory before proceeding.

```bash
if [ ! -d /var/lib/jenkins ]; then
  sudo install -d -o jenkins -g jenkins -m 0750 /var/lib/jenkins
fi
sudo systemctl reset-failed jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins --no-pager
```

If Jenkins still fails, inspect its recent service logs:

```bash
sudo journalctl -u jenkins -n 50 --no-pager
```

## 5. Create and run the Jenkins pipeline

1. In Jenkins, select **New Item**.
2. Enter a name such as `deployment-demo`, select **Pipeline**, and select **OK**.
3. Under **Pipeline**, choose **Pipeline script from SCM**.
4. Select **Git** and enter the repository URL shown by `git remote -v`. Add Jenkins credentials if the repository is private.
5. Set the branch to `*/master` and the script path to `Jenkinsfile`, then select **Save**.
6. Select **Build Now**. Open the build and select **Console Output**.
7. Confirm that the **Test**, **Package**, and **Deploy and verify** stages complete successfully.

The Jenkins agent needs Java, Maven, and curl available on its PATH. The application runs on that agent and listens on its localhost port 8081. If Jenkins is on this computer, open <http://localhost:8081> after the build succeeds. If the agent is on another machine, access the page on that machine or through a secure tunnel.

**Screenshot 5:** Capture the successful Jenkins stages or console output.

**Screenshot 6:** Capture the application page after the Jenkins deployment.

## 6. Record the comparison

Use the same project revision and similar conditions for both methods. For each method, perform three trials:

1. Start a stopwatch immediately before the build starts. For Jenkins, start it when you select **Build Now**.
2. Stop the stopwatch when the deployed application responds to its health check.
3. Record the elapsed time, any failures, and the time you actively spent entering commands or clicking buttons.
4. Calculate the average: `(trial 1 + trial 2 + trial 3) / 3`.

For recovery time, record how long it takes to restore a working deployment after a controlled failure. Write **Not tested** if you did not perform a recovery test. Do not invent results or assume that the two methods will have the same timings.

## Project files

```text
pom.xml                             Maven build configuration
Jenkinsfile                         Jenkins pipeline
scripts/deploy.sh                   Automated deployment and health check
src/main/java/com/example/devops/    Java application
src/test/java/com/example/devops/    JUnit test
.gitignore                          Files excluded from Git
```

## Troubleshooting

- **Port 8081 is already in use:** stop the application you previously started, or configure a different `PORT` for both the application and health check.
- **The manual application does not start:** inspect `cat "$HOME/devops-project3-manual/app.log"`.
- **The Jenkins deployment does not start:** check the build's Console Output and the `app.log` under the Jenkins agent user's `~/devops-project3-jenkins` directory.
- **Jenkins cannot find Maven or Java:** install them on the Jenkins agent and make them available to the Jenkins service user.
- **Git push requests authentication:** use the authentication method configured for your GitHub account. Do not put access tokens in this README or commit them to Git.
