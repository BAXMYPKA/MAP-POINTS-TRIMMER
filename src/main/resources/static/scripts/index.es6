(function () {

        const userMessage = document.querySelector("#userMessage");
        const previewSize = document.getElementById("previewSize");
        const previewSizeUnits = document.getElementById("previewSizeUnits");
        let adminLoginCounter = 0;

        if (typeof singleUserMode === 'undefined' || singleUserMode == null) {
            singleUserMode = true;
        }
        //2160 = 2 hours, 1620 = 1.5 hours, 1080 = 1 hour of session idle for single- or multi-user server mode
        const sessionInterval = singleUserMode ? 1620 : 1080;

        let userMessageClose = document.querySelector(".specialButtons_close");
        if (userMessageClose !== null) {
            userMessageClose.addEventListener('click', ev => {
                userMessage.innerHTML = "";
                userMessage.className = "userMessage.hidden";

            });
        }

        function showUserMessage(text) {
            userMessage.innerHTML = text;
            userMessage.className = "userMessage";
        }

        function showWarningUserMessage(text) {
            userMessage.innerHTML = text;
            userMessage.className = "userMessage warningUserMessage";
        }

        function hideUserMessage() {
            userMessage.innerHTML = "";
            userMessage.className = "userMessage.hidden";
        }

        function getUserMessage() {
            return userMessage.innerHTML;
        }

        setInterval(function () {
            document.getElementById("pictogramValueTrace").value =
                document.querySelector("input[name='pictogram']:checked").value;
        }, 1500);

        if (typeof isShutDown !== 'undefined' && !isShutDown) {

            let intervalCounter = 0;

            let beaconIntervalId = setInterval(function () {
                navigator.sendBeacon(serverAddress.concat("/beacon"), null);
                intervalCounter++;
                checkInterval(beaconIntervalId);
            }, 8000);

            //After 2 hours it will stop sending beacons
            function checkInterval(beaconIntervalId) {
                if (intervalCounter > sessionInterval) {
                    clearInterval(beaconIntervalId);
                }
            }
        }

        //This will fire even on refreshing, closing a tab or a browser
        if (typeof serverAddress !== 'undefined' && serverAddress) {
            window.onbeforeunload = function () {
                navigator.sendBeacon(serverAddress.concat("/stop"), null);
                return null;

            }
        }

        /**
         *
         * @param level TRACE, DEBUG, INFO, ERROR, WARN
         */
        let setLoggingLevel = function (level) {
            let logLevel = {
                "configuredLevel": level
            }
            if (typeof serverAddress !== 'undefined' && serverAddress) {
                fetch(serverAddress.concat("/actuator/loggers/mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication"), {
                    method: "POST",
                    body: JSON.stringify(logLevel),
                    headers: {
                        "Content-Type": "application/json;charset=utf-8"
                    }
                });
            }
        };

        window.onload = setLoggingLevel("WARN");

        document.querySelector(".rightHeaderGroup__shutdownButtonOn_img").addEventListener('click', ev => {
            if (singleUserMode) {
                ev.preventDefault();
                window.location.href = serverAddress.concat('/shutdown');
            }
        });

        document.querySelector(".mainHeader__logoImg").addEventListener('click', ev => {
            window.location.href = serverAddress;
        });

        document.getElementById('setPreviewSize').addEventListener('change', ev => {
                if (ev.target.checked) {
                    previewSize.disabled = false;
                    previewSizeUnits.disabled = false;
                } else {
                    previewSize.disabled = true;
                    previewSizeUnits.disabled = true;
                }
            }
        );

        document.getElementById("previewSizeUnits").addEventListener('change', ev => {
            if (ev.target.value === "percentage") {
                previewSize.value = "100";
            } else if (ev.target.value === "pixels") {
                previewSize.value = "600";
            }
        });

        document.querySelectorAll(".interrogation").forEach(value => {
            value.addEventListener('click', evt => {
                if (getUserMessage() === evt.target.getAttribute("title")) {
                    //To hide the description if same interrogation is clicked
                    hideUserMessage();
                    document.querySelectorAll(".interrogation").forEach(interrogation => {
                        interrogation.style.backgroundColor = "limegreen";
                    });
                } else {
                    //To show the interrogation description in userMessage innerHtml
                    showUserMessage(evt.target.getAttribute("title"));
                    evt.target.style.backgroundColor = "greenyellow";
                    document.querySelectorAll(".interrogation").forEach(interrogation => {
                        if (interrogation !== evt.target) {
                            interrogation.style.backgroundColor = "limegreen";
                        }
                    });
                }
            });
        });

        document.getElementById("locusFile").addEventListener('change', ev => {
            if (ev.target.files[0].size / 1024 / 1024 > maxFileSizeMb) {
                showWarningUserMessage("Max file size = " + maxFileSizeMb + "Mb!");
            } else {
                hideUserMessage();
            }
        });

        document.getElementById("setPath").addEventListener('change', ev => {
            const path = document.getElementById("path");
            const pathTypes = document.querySelectorAll("input[name='pathType']");
            if (ev.target.checked) {
                path.disabled = false;
                pathTypes.forEach(value => {
                    value.disabled = false
                    if (value.id === "webPath" && value.checked) {
                        document.getElementById("asAttachmentInLocus").disabled = true;
                    }
                });
            } else {
                path.disabled = true;
                pathTypes.forEach(value => {
                    value.disabled = true
                    document.getElementById("asAttachmentInLocus").disabled = false;
                });
            }
        });

        const elementsByName = document.getElementsByName("pathType");
        const pathTypes = Array.from(elementsByName);
        pathTypes.forEach(pathType => {
            pathType.addEventListener('change', ev => {
                const asAttachmentInLocus = document.getElementById("asAttachmentInLocus");
                if (ev.target.getAttribute("id") === "webPath") {
                    asAttachmentInLocus.disabled = true;
                } else {
                    asAttachmentInLocus.disabled = false;
                }

            });
        });

        document.getElementById("trim").addEventListener('click', ev => {
            //Checks for all the inputs on page for HTML5 inner validation
            for (const value of document.querySelectorAll("input")) {
                if (!value.checkValidity()) {
                    return;
                }
            }
            document.querySelector('.loadForm').submit();
            document.querySelector(".downloadMessage").hidden = false;
        });

        document.getElementById("replaceLocusIcons").addEventListener('change', ev => {
            const pictogramDropdownDiv = document.getElementById("pictogram-dropdown");
            const pictogramValueTrace = document.getElementById("pictogramValueTrace");
            if (ev.target.checked) {
                pictogramDropdownDiv.style.display = "inline-block"
                pictogramValueTrace.disabled = false;
            } else {
                pictogramDropdownDiv.style.display = "none"
                pictogramValueTrace.disabled = true;
            }
        });


        document.getElementById("setPointIconSize").addEventListener('change', ev => {
            const pointsIconSizeInput = document.getElementById("pointIconSize");
            if (ev.target.checked) {
                pointsIconSizeInput.disabled = false;
            } else {
                pointsIconSizeInput.disabled = true;
            }
        });

        document.getElementById("setPointIconOpacity").addEventListener('change', ev => {
            const pointsIconSizeInput = document.getElementById("pointIconOpacity");
            if (ev.target.checked) {
                pointsIconSizeInput.disabled = false;
            } else {
                pointsIconSizeInput.disabled = true;
            }
        });

        document.getElementById("setPointTextSize").addEventListener('change', ev => {
            const pointsTextSizeInput = document.getElementById("pointTextSize");
            if (ev.target.checked) {
                pointsTextSizeInput.disabled = false;
            } else {
                pointsTextSizeInput.disabled = true;
            }
        });

        document.getElementById("setPointTextHexColor").addEventListener('change', ev => {
            const pointsTextColorInput = document.getElementById("pointTextHexColor");
            if (ev.target.checked) {
                pointsTextColorInput.disabled = false;
            } else {
                pointsTextColorInput.disabled = true;
            }
        });

        document.getElementById("setPointTextOpacity").addEventListener('change', ev => {
            const pointTextTransparencyInput = document.getElementById("pointTextOpacity");
            if (ev.target.checked) {
                pointTextTransparencyInput.disabled = false;
            } else {
                pointTextTransparencyInput.disabled = true;
            }
        });

        document.getElementById("setPointIconSizeDynamic").addEventListener('change', ev => {
            const pointsIconSizeInput = document.getElementById("pointIconSizeDynamic");
            if (ev.target.checked) {
                pointsIconSizeInput.disabled = false;
            } else {
                pointsIconSizeInput.disabled = true;
            }
        });

        document.getElementById("setPointIconOpacityDynamic").addEventListener('change', ev => {
            const pointsIconSizeInput = document.getElementById("pointIconOpacityDynamic");
            if (ev.target.checked) {
                pointsIconSizeInput.disabled = false;
            } else {
                pointsIconSizeInput.disabled = true;
            }
        });

        document.getElementById("setPointTextSizeDynamic").addEventListener('change', ev => {
            const pointsTextSizeInput = document.getElementById("pointTextSizeDynamic");
            if (ev.target.checked) {
                pointsTextSizeInput.disabled = false;
            } else {
                pointsTextSizeInput.disabled = true;
            }
        });

        document.getElementById("setPointTextHexColorDynamic").addEventListener('change', ev => {
            const pointsTextColorInput = document.getElementById("pointTextHexColorDynamic");
            if (ev.target.checked) {
                pointsTextColorInput.disabled = false;
            } else {
                pointsTextColorInput.disabled = true;
            }
        });

        document.getElementById("setPointTextOpacityDynamic").addEventListener('change', ev => {
            const pointTextTransparencyInput = document.getElementById("pointTextOpacityDynamic");
            if (ev.target.checked) {
                pointTextTransparencyInput.disabled = false;
            } else {
                pointTextTransparencyInput.disabled = true;
            }
        });

        const articleAbout = document.querySelector(".articleAbout");

        articleAbout.addEventListener('click', evt => {
            if (articleAbout.style.display === "none") {
                articleAbout.style.display = "block";
            } else {
                articleAbout.style.display = "none";
            }
        });

        document.querySelector("#aboutItem").addEventListener('click', ev => {
            if (articleAbout.style.display === "none") {
                articleAbout.style.display = "block";
            } else {
                articleAbout.style.display = "none";
            }
        });

        document.getElementById("debugMode").addEventListener('change', ev => {
            let debugInterrogation = document.getElementById("debugInterrogation");
            if (ev.target.checked) {
                if (confirm(debugInterrogation.title)) {
                    setLoggingLevel("INFO");
                } else {
                    ev.target.checked = false;
                }
            } else {
                setLoggingLevel("WARN");
            }
        });

        document.getElementById("filterOut").addEventListener('click', ev => {
            let downloadMessage;
            //Checks all the filter inputs within the fieldset for HTML5 inner validation
            for (const child of document.getElementById('fieldSetFilter').children) {
                if (child.tagName === "INPUT" && !child.checkValidity()) {
                    return;
                } else if (child.className === "downloadMessage") {
                    downloadMessage = child;
                }
            }
            document.querySelector('.filterLoadForm').submit();
            downloadMessage.hidden = false;
        });

        //TODO: to set the "fieldset id="adminFieldset" as hiden

        document.getElementById("mainHeader__logoImg").addEventListener('click', ev => {
            adminLoginCounter++;
            if (adminLoginCounter === 3) {
                document.getElementById("adminFieldset").hidden = false;
                //TODO: to check
                ev.currentTarget.title = "Admin opened";
            }
        });

        document.getElementById("adminVerifyButton").addEventListener('click', ev => {
            let adminLogin = document.getElementById("adminLogin").value;
            let adminPassword = document.getElementById("adminPassword").value;
            verifyAdminCredentials(adminLogin, adminPassword);
        });

        let verifyAdminCredentials = function (login, password) {
            if ((!login || login.length === 0) || (!password || password.length === 0)) return;
            let adminCredentials = {
                'login': login,
                'password': password
            }
            fetch(serverAddress.concat("/admin"), {
                method: "POST",
                body: JSON.stringify(adminCredentials),
                headers: {
                    "Content-Type": "application/json;charset=utf-8"
                }
            }).then(function (response) {
                if (!response.ok) {
                    response.text().then(text => showWarningUserMessage(text));

                } else {
                    response.text().then(text => showUserMessage(text));
                    console.log(response, "OK");
                }
            });
        };
    }
)();