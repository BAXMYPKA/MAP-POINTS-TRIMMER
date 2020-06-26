(function localPresets() {
		
		class Preset {
			constructor(presetName, trimXml, trimDescriptions, previewSize, setPath, relativePath, absolutePath, webPath, path,
						asAttachmentInLocus, clearDescriptions, pointIconSize, pointTextSize, pointTextOpacity, pointTextHexColor,
						pointIconSizeDynamic, pointTextSizeDynamic, pointTextOpacityDynamic, pointTextHexColorDynamic) {
				this.presetName = presetName;
				this.trimXml = trimXml;
				this.trimDescriptions = trimDescriptions;
				this.previewSize = previewSize;
				this.setPath = setPath;
				this.relativePath = relativePath;
				this.absolutePath = absolutePath;
				this.webPath = webPath;
				this.path = path;
				this.asAttachmentInLocus = asAttachmentInLocus;
				this.clearDescriptions = clearDescriptions;
				this.pointIconSize = pointIconSize;
				this.pointTextSize = pointTextSize;
				this.pointTextOpacity = pointTextOpacity;
				this.pointTextHexColor = pointTextHexColor;
				this.pointIconSizeDynamic = pointIconSizeDynamic;
				this.pointTextSizeDynamic = pointTextSizeDynamic;
				this.pointTextOpacityDynamic = pointTextOpacityDynamic;
				this.pointTextHexColorDynamic = pointTextHexColorDynamic;
			}
			
			constructFromInputs() {
				this.trimXml = document.getElementById("trimXml").value;
				this.trimDescriptions = document.getElementById("trimDescriptions").value;
				this.previewSize = document.getElementById("previewSize").value;
				this.setPath = document.getElementById("setPath").value;
				this.relativePath = document.getElementById("relativePath").value;
				this.absolutePath = document.getElementById("absolutePath").value;
				this.webPath = document.getElementById("webPath").value;
				this.path = document.getElementById("path").value;
				this.asAttachmentInLocus = document.getElementById("asAttachmentInLocus").value;
				this.clearDescriptions = document.getElementById("clearDescriptions").value;
				this.pointIconSize = document.getElementById("pointIconSize").value;
				this.pointTextSize = document.getElementById("pointTextSize").value;
				this.pointTextOpacity = document.getElementById("pointTextOpacity").value;
				this.pointTextHexColor = document.getElementById("pointTextHexColor").value;
				this.pointIconSizeDynamic = document.getElementById("pointIconSizeDynamic").value;
				this.pointTextSizeDynamic = document.getElementById("pointTextSizeDynamic").value;
				this.pointTextOpacityDynamic = document.getElementById("pointTextOpacityDynamic").value;
				this.pointTextHexColorDynamic = document.getElementById("pointTextHexColorDynamic").value;
			}
		};
		
		const MAX_PRESETS_NUMBER = 4;
		const PRESETS_KEY = "presets";
		const presetsDatalistInput = document.getElementById("presetsInput");
		
		document.getElementById("presetClear").addEventListener('click', ev => {
			presetsDatalistInput.value = null;
		});
		
		presetsDatalistInput.addEventListener("input", ev => {
			if (ev.target.value === "...") {
				ev.target.value = null;
			}
		});
		
		document.getElementById("presetSave").addEventListener("click", ev => {
			let newPresetName = presetsDatalistInput.value;
			if (newPresetName === null || newPresetName === "") {
				return;
			}
			addPresetToLocalStorage(newPresetName);
		});
		
		document.getElementById("presetDelete").addEventListener("click", ev => {
			let existingPresetName = presetsDatalistInput.value;
			if (existingPresetName === null || existingPresetName === "") {
				return;
			}
			deletePresetFromLocalStorage(existingPresetName);
		});
		
		function getPresetsArrayFromLocalStorage() {
			let presets = JSON.parse(localStorage.getItem(PRESETS_KEY));
			return presets !== null ? presets : [];
		};
		
		function addPresetToLocalStorage(presetName) {
			let presets = getPresetsArrayFromLocalStorage();
			let presetsUpdated = false;
			for (let existingPreset of presets) {
				if (existingPreset.presetName === presetName) {
					let preset = Object.assign(new Preset(), existingPreset);
					preset.constructFromInputs();
					presetsUpdated = true;
				}
			}
			if (!presetsUpdated) {
				//No Preset with same name, add a new one
				if (presets.length === MAX_PRESETS_NUMBER) {
					presetsDatalistInput.value = "Max presets number exceeded!"
					return;
				}
				let preset = new Preset();
				preset.presetName = presetName;
				preset.constructFromInputs();
				presets.push(preset);
			}
			localStorage.setItem(PRESETS_KEY, JSON.stringify(presets));
			setPresetsToDatalist();
		}
		
		function deletePresetFromLocalStorage(presetName) {
			let filteredPresets = getPresetsArrayFromLocalStorage()
				.filter(existingPreset => existingPreset.presetName !== presetName);
			localStorage.setItem(PRESETS_KEY, JSON.stringify(filteredPresets));
			setPresetsToDatalist();
			presetsDatalistInput.value = null;
		}
		
		/**
		 * 'presets' from the localStorage is the JSONed Array of Preset classes
		 */
		function setPresetsToDatalist() {
			const presetsDatalist = document.getElementById("presetsDatalist");
			if (localStorage.getItem(PRESETS_KEY) != null) {
				presetsDatalist.innerHTML = "";
				for (let preset of getPresetsArrayFromLocalStorage()) {
					let option = document.createElement("option");
					option.value = preset.presetName;
					presetsDatalist.appendChild(option);
				}
			}
		};
		setPresetsToDatalist();
	}
)();