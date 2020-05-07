(function () {
		
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
		
		document.getElementById("setPath").addEventListener('change', ev => {
			const path = document.getElementById("path");
			if (ev.target.checked) {
				path.disabled = false;
			} else {
				path.disabled = true;
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