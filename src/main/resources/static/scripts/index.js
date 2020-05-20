(function () {
		
		const userMessage = document.querySelector("#userMessage");
		
		let userMessageClose = document.querySelector(".userMessage__close");
		if (userMessageClose !== null) {
			userMessageClose.addEventListener('click', ev => {
				userMessage.innerHTML = "";
				userMessage.className = "userMessage.hidden";
				
			});
		}
		
		document.querySelector(".rightHeaderGroup__shutdownButtonOn_img").addEventListener('click', (ev => {
			ev.preventDefault();
			window.location.href = serverAddress.concat('/shutdown');
		}));
		
		document.querySelector(".mainHeader__logoImg").addEventListener('click', ev => {
			window.location.href = serverAddress;
		});
		
		document.getElementById('setPreviewSize').addEventListener('change', ev => {
				const previewSize = document.getElementById("previewSize");
				if (ev.target.checked) {
					previewSize.disabled = false;
				} else {
					previewSize.disabled = true;
				}
			}
		);
		
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
		
		document.getElementById("trim").addEventListener('click', ev => {
			if (document.getElementById('locusFile').files.length === 0) {
				return;
			} else {
				document.querySelector('.downloadMessage').hidden = false;
				// document.querySelector('.awaitGif').hidden = false;
				document.querySelector('.loadForm').submit();
				document.getElementById("locusFile").value = null;
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
		
	}
)();