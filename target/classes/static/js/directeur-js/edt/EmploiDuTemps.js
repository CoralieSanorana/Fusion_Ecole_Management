// Global init function we can call when new content is loaded
function initEmploiDuTemps() {
    const grid = document.querySelector('[data-edt-grid]');
    const form = document.querySelector('[data-edt-form]');

    if (!grid || !form) {
        return;
    }

    const selectedCount = form.querySelector('[data-selected-count]');
    const selectedList = form.querySelector('[data-selected-list]');
    const selectedCells = form.querySelector('[data-selected-cells]');
    const clearButton = form.querySelector('[data-clear-selection]');
    const submitButton = form.querySelector('button[type="submit"]');
    const cellButtons = Array.from(grid.querySelectorAll('[data-edt-cell]'));
    const selected = new Map();

    const setSelectedState = (button, isSelected) => {
        button.classList.toggle('is-selected', isSelected);
        button.setAttribute('aria-pressed', isSelected ? 'true' : 'false');
    };

    const syncSelection = () => {
        if (selectedCells) {
            selectedCells.innerHTML = '';
            selected.forEach((value) => {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'cells';
                input.value = value.value;
                selectedCells.appendChild(input);
            });
        }

        if (selectedList) {
            selectedList.innerHTML = '';
            if (selected.size === 0) {
                const empty = document.createElement('span');
                empty.className = 'edt-chip is-empty';
                empty.textContent = 'Aucune case';
                selectedList.appendChild(empty);
            } else {
                selected.forEach((item) => {
                    const chip = document.createElement('span');
                    chip.className = 'edt-chip';
                    chip.textContent = item.label;
                    selectedList.appendChild(chip);
                });
            }
        }

        if (selectedCount) {
            const count = selected.size;
            selectedCount.textContent = `${count} case${count > 1 ? 's' : ''}`;
        }

        const triggerBadge = document.getElementById('selected-badge');
        if (triggerBadge) {
            const count = selected.size;
            if (count > 0) {
                triggerBadge.textContent = count;
                triggerBadge.style.display = 'inline-flex';
            } else {
                triggerBadge.style.display = 'none';
            }
        }

        if (submitButton) {
            submitButton.disabled = selected.size === 0;
        }
    };

    cellButtons.forEach((button) => {
        const cellValue = button.getAttribute('data-cell-value');
        const cellLabel = button.getAttribute('data-cell-label') || '';

        button.addEventListener('click', () => {
            if (!cellValue) {
                return;
            }

            if (selected.has(cellValue)) {
                selected.delete(cellValue);
                setSelectedState(button, false);
            } else {
                selected.set(cellValue, { value: cellValue, label: cellLabel });
                setSelectedState(button, true);
            }

            syncSelection();
        });
    });

    if (clearButton) {
        clearButton.addEventListener('click', () => {
            selected.clear();
            cellButtons.forEach((button) => setSelectedState(button, false));
            syncSelection();
        });
    }

    form.addEventListener('submit', (event) => {
        if (selected.size === 0) {
            event.preventDefault();
            alert('Sélectionnez au moins une case avant d\'enregistrer.');
        }
    });

    syncSelection();
}

// Initialize immediately when first loaded
initEmploiDuTemps();
