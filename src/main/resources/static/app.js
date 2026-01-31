const statusEl = document.getElementById('saveStatus');

const setStatus = (text, state) => {
  if (!statusEl) return;
  statusEl.textContent = text;
  statusEl.dataset.state = state || '';
};

const updateBar = (input) => {
  const targetId = input.dataset.barTarget;
  if (!targetId) return;
  const bar = document.getElementById(targetId);
  if (!bar) return;
  const value = Number(input.value);
  if (Number.isNaN(value)) return;
  const clamped = Math.max(0, Math.min(100, value));
  bar.style.width = `${clamped}%`;
};

const applyValue = (input, value) => {
  if (input.type === 'checkbox') {
    input.checked = value === true || value === 'true' || value === 1 || value === '1';
  } else {
    input.value = value;
  }
  updateBar(input);
};

const readInputs = () => Array.from(document.querySelectorAll('[data-setting-key]'));

const loadSettings = async () => {
  const inputs = readInputs();
  if (!inputs.length) return;

  setStatus('設定を読み込み中...', 'loading');
  let settings = {};

  try {
    const res = await fetch('/api/settings');
    if (!res.ok) throw new Error('Failed to load settings');
    settings = await res.json();
    setStatus('設定を読み込みました', 'ready');
  } catch (err) {
    setStatus('設定の取得に失敗しました (オフライン)', 'error');
  }

  inputs.forEach((input) => {
    const key = input.dataset.settingKey;
    let value = settings[key];

    if (value === undefined) {
      if (input.dataset.default !== undefined) {
        value = input.dataset.default;
      } else if (input.type === 'checkbox') {
        value = input.checked;
      } else {
        value = input.value;
      }
    }

    applyValue(input, value);
  });

  inputs.forEach((input) => {
    const save = async () => {
      const key = input.dataset.settingKey;
      const value = input.type === 'checkbox' ? input.checked : input.value;

      updateBar(input);
      setStatus('保存中...', 'saving');
      try {
        const res = await fetch(`/api/settings/${encodeURIComponent(key)}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ value }),
        });

        if (!res.ok) throw new Error('Failed to save');
        setStatus('保存しました', 'saved');
      } catch (err) {
        setStatus('保存に失敗しました', 'error');
      }
    };

    input.addEventListener('change', save);
    input.addEventListener('input', () => {
      updateBar(input);
      if (['text', 'number', 'time', 'datetime-local'].includes(input.type)) {
        setStatus('未保存の変更があります', 'dirty');
      }
    });
  });
};

document.addEventListener('DOMContentLoaded', loadSettings);
