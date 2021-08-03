(function () {

		window.addEventListener("beforeunload", function (event) {
			navigator.sendBeacon(serverAddress.concat("/shutdown"));
		});

		document.querySelector(".rightHeaderGroup__shutdownButtonOn_img").addEventListener('click', ev => {
			ev.preventDefault();
			window.location.href = serverAddress.concat('/shutdown');
		});
		
		document.querySelector(".mainHeader__logoImg").addEventListener('click', ev => {
			window.location.href = serverAddress;
		});
		
		document.getElementById("clickHere").addEventListener('click', ev => {
			ev.preventDefault();
			let errorDetails = document.getElementById("errorDetails");
			errorDetails.open = true; //Otherwise it won't be ranged for being copied
			let closableSection = document.getElementById("closableSection");
			let range = document.createRange();
			range.selectNode(closableSection);
			window.getSelection().removeAllRanges();
			window.getSelection().addRange(range);
			let copied = document.execCommand('copy');
			if (!copied) {
				alert("Unable to copy the text!");
			}
			window.getSelection().removeAllRanges();
			errorDetails.open = false;
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