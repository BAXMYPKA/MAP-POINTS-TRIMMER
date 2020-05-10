(function () {
		
		const userMessage = document.querySelector(".userMessage");
		
		document.querySelector(".shutdownButtonOn").addEventListener('click', (ev => {
			ev.preventDefault();
			window.location.href = window.location.href.concat('shutdown');
		}));
		
		document.getElementById('setPreviewSize').addEventListener('change', ev => {
				const previewSize = document.getElementById("previewSize");
				if (ev.target.checked) {
					previewSize.disabled = false;
				} else {
					previewSize.disabled = true;
				}
				;
			}
		);
		
		document.querySelectorAll(".interrogation").forEach(value => {
			value.addEventListener('click', evt => {
				userMessage.innerHTML = evt.target.getAttribute("title");
			})
		});
		
		document.getElementById("locusFile").addEventListener('change', ev => {
			if (ev.target.files[0].size/1024/1024 > maxFileSizeMb) {
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
		
		document.getElementById("convert").addEventListener('click', ev => {
			if (document.getElementById('locusFile').files.length === 0) {
				return;
			} else {
				document.querySelector('.downloadMessage').hidden = false;
				// document.querySelector('.awaitGif').hidden = false;
				document.querySelector('.loaderForm').submit();
			}
		});
	}
)();