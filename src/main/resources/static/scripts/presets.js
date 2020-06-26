(function presets() {
		
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
		const presetsDatalist = document.getElementById("presets");
		const presetSaveButton = document.getElementById("presetSave");
		const presetDeleteButton = document.getElementById("presetDelete");
		
		presetsDatalist.addEventListener("change", ev => {
			if (ev.target.value === "...") {
				ev.target.value = null;
			}
		});
		
		presetSaveButton.addEventListener("click", ev => {
			let newPresetName = presetsDatalist.value;
			if (newPresetName === null || newPresetName === "") {
				return;
			}
			addPresetToLocalStorage(newPresetName);
		});
		
		presetDeleteButton.addEventListener("click", ev => {
			let existingPresetName = presetsDatalist.value;
			if (existingPresetName === null || existingPresetName === "") {
				return;
			}
			deletePresetFromLocalStorage(existingPresetName);
		});
		
		function getPresetsArrayFromLocalStorage() {
			let presets = localStorage.getItem("presets");
			return presets !== null ? presets : [];
		};
		
		function addPresetToLocalStorage(presetName) {
			let presetsUpdated = false;
			getPresetsArrayFromLocalStorage().forEach(existingPreset => {
				if (existingPreset.presetName === presetName) {
					existingPreset.constructFromInputs();
					presetsUpdated = true;
				}
			});
			if (!presetsUpdated) {
				//No Preset with same name, add a new one
				if (getPresetsArrayFromLocalStorage().length === MAX_PRESETS_NUMBER) {
					presetsDatalist.value = "Max presets number exceeded!"
					return;
				}
				
				let preset = new Preset();
				preset.constructFromInputs();
				getPresetsArrayFromLocalStorage().push(preset);
			}
			localStorage.setItem("presets", JSON.stringify(presets));
		}
		
		function deletePresetFromLocalStorage(presetName) {
			let filteredPresets = getPresetsArrayFromLocalStorage()
				.filter(existingPreset => existingPreset.presetName !== presetName);
			localStorage.setItem("presets", JSON.stringify(filteredPresets));
			presetsDatalist.value = null;
		}
		
		/**
		 * 'presets' from the localStorage is the JSONed Array of Preset classes
		 */
		(function setPresetsToDatalist() {
			// presets = localStorage.getItem("presets");
			if (localStorage.getItem("presets") != null) {
				localStorage.getItem("presets").forEach(preset => {
					let option = document.createElement("option");
					option.value = preset.presetName;
					presetsDatalist.appendChild(option);
				});
			}
		})();
	}
)();