(function () {
		
		document.querySelector(".rightHeaderGroup__shutdownButtonOn_img").addEventListener('click', ev => {
			ev.preventDefault();
			window.location.href = serverAddress.concat('/shutdown');
		});
		
		document.querySelector(".mainHeader__logoImg").addEventListener('click', ev => {
			window.location.href = serverAddress;
		});
		
		document.getElementById("clickHere").addEventListener('click', ev => {
			ev.preventDefault();
			let closableSection = document.getElementById("closableSection");
			let range = document.createRange();
			range.selectNode(closableSection);
			window.getSelection().addRange(range);
			let copied = document.execCommand('copy');
			if (!copied) {
				alert("Unable to copy the text!");
			}
		});
		
		document.querySelector(".article__section_closed").addEventListener('click', ev => {
			if (document.getElementById("closableSection").classList.contains("article__section_closed")) {
				document.getElementById("closableSection").classList.toggle("article__section_closed");
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