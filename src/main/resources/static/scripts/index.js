(function () {
		
		const userMessage = document.querySelector("#userMessage");
		const previewSize = document.getElementById("previewSize");
		
		let userMessageClose = document.querySelector(".userMessage__close");
		if (userMessageClose !== null) {
			userMessageClose.addEventListener('click', ev => {
				userMessage.innerHTML = "";
				userMessage.className = "userMessage.hidden";
				
			});
		}
		
		/**
		 *
		 * @param level TRACE, DEBUG, INFO, ERROR, WARN
		 */
		let setLoggingLevel = function (level) {
			let logLevel = {
				"configuredLevel": level
			}
			let setInfoLogLevel = fetch(serverAddress.concat("/actuator/loggers/mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication"), {
				method: "POST",
				body: JSON.stringify(logLevel),
				headers: {
					"Content-Type": "application/json;charset=utf-8"
				}
			});
		};
		
		window.onload = setLoggingLevel("WARN");
		
		document.querySelector(".rightHeaderGroup__shutdownButtonOn_img").addEventListener('click', (ev => {
			ev.preventDefault();
			window.location.href = serverAddress.concat('/shutdown');
		}));
		
		document.querySelector(".mainHeader__logoImg").addEventListener('click', ev => {
			window.location.href = serverAddress;
		});
		
		document.getElementById('setPreviewSize').addEventListener('change', ev => {
				if (ev.target.checked) {
					previewSize.disabled = false;
				} else {
					previewSize.disabled = true;
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
		document.querySelector(".article__section_closed").addEventListener('click', ev => {
			if (document.getElementById("closableSection").classList.contains("article__section_closed")) {
				document.getElementById("closableSection").classList.toggle("article__section_closed");
			}
		});
		
		document.querySelectorAll(".interrogation").forEach(value => {
			value.addEventListener('click', evt => {
				if (userMessage.innerHTML === evt.target.getAttribute("title")) {
					//To hide the description if same interrogation is clicked
					userMessage.innerHTML = "";
					userMessage.className = "userMessage.hidden";
					document.querySelectorAll(".interrogation").forEach(interrogation => {
						interrogation.style.backgroundColor = "limegreen";
					});
				} else {
					//To show the interrogation description in userMessage innerHtml
					userMessage.innerHTML = evt.target.getAttribute("title");
					userMessage.className = "userMessage";
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
				userMessage.innerHTML = "Max file size = " + maxFileSizeMb + "Mb!";
			} else {
				userMessage.innerHTML = "";
			}
		});
		
		document.getElementById("setPath").addEventListener('change', ev => {
			const path = document.getElementById("path");
			const pathTypes = document.querySelectorAll("input[name='pathType']");
			if (ev.target.checked) {
				path.disabled = false;
				pathTypes.forEach(value => value.disabled = false);
			} else {
				path.disabled = true;
				pathTypes.forEach(value => value.disabled = true);
			}
		});
		
		document.getElementsByName("pathType").forEach(pathType => {
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
		});
		
		document.getElementById("setPointIconSize").addEventListener('change', ev => {
			const pointsIconSizeInput = document.getElementById("pointIconSize");
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
		
		document.getElementById("setPointTextColor").addEventListener('change', ev => {
			const pointsTextColorInput = document.getElementById("pointTextColor");
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
			if (ev.target.checked) {
				setLoggingLevel("INFO");
			} else {
				setLoggingLevel("WARN");
			}
		});
	}
)();