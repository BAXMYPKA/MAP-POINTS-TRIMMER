(function localPresets() {
		
		const MAX_PRESETS_NUMBER = 4;
		const PRESETS_KEY = "presets";
		const presetsDatalistInput = document.getElementById("presetsInput");
		
		class Preset {
			constructor(presetName, inputsArray) {
				this.presetName = presetName;
				this.inputsArray = inputsArray;
			}
		}
		
		function getInputs() {
			let formElements = document.getElementById("poiFile").elements;
			let inputs = [];
			for (let element of formElements) {
				if (element.tagName.match(new RegExp("input", "i")) || element.tagName.match(new RegExp("select", "i"))) {
					if (element.type === "file" || element.type === "submit") continue;
					inputs.push({
						id: element.id,
						value: element.value,
						checked: element.checked,
						disabled: element.disabled
					});
				}
			}
			return inputs;
		}
		
		function setInputs(presetName) {
			getPresetsArrayFromLocalStorage().filter(preset => preset.presetName === presetName).forEach(currentPreset => {
				for (let presetInput of currentPreset.inputsArray) {
					let formInput = document.getElementById(presetInput.id);
					formInput.value = presetInput.value;
					formInput.checked = presetInput.checked;
					formInput.disabled = presetInput.disabled;
				}
			});
		}
		
		function clearInputs() {
			let formElements = document.getElementById("poiFile").elements;
			for (let input of formElements) {
				if (input.tagName.match(new RegExp("input", "i")) || input.tagName.match(new RegExp("select", "i"))) {
					if (input.type === "file" || input.type === "submit") continue;
					if (input.type === "number" || input.type === "text") {
						input.value = null;
						input.disabled = true;
					}
					if (input.type === "color") {
						input.value = "#000000";
						input.disabled = true;
					}
					if (input.type === "checkbox") {
						input.checked = false;
					}
					if (input.type === "radio") {
						input.disabled = true;
						document.getElementById("asAttachmentInLocus").disabled = false;
					}
				}
			}
		}
		
		document.getElementById("presetClear").addEventListener('click', ev => {
			presetsDatalistInput.value = null;
			clearInputs();
		});
		
		presetsDatalistInput.addEventListener("input", ev => {
			for (let input of getPresetsArrayFromLocalStorage()) {
				if (ev.target.value === input.presetName) {
					setInputs(input.presetName);
					break;
				}
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
			let presets = getPresetsArrayFromLocalStorage().filter(preset => preset.presetName !== presetName);
			if (presets.length === MAX_PRESETS_NUMBER) {
				presetsDatalistInput.value = `MAX PRESETS=${MAX_PRESETS_NUMBER}`;
				return;
			}
			let preset = new Preset(presetName, getInputs());
			presets.push(preset);
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