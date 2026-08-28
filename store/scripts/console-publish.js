// Run via: ego-browser nodejs < store/scripts/console-publish.js
// Env: PLAY_AAB, PLAY_LABEL (e.g. "7 (0.7.0)"), RELEASE_NOTES
// Helpers (useOrCreateTaskSpace, click, ...) are injected by ego-browser.

const TRACK =
  'https://play.google.com/console/u/0/developers/5934139594166642747/app/4974554092638251166/tracks/internal-testing'
const AAB = process.env.PLAY_AAB
const LABEL = process.env.PLAY_LABEL
const NOTES = process.env.RELEASE_NOTES || 'Internal test update.'

if (!AAB || !LABEL) {
  cliLog('need PLAY_AAB and PLAY_LABEL')
  throw new Error('need PLAY_AAB and PLAY_LABEL')
}

const task = await useOrCreateTaskSpace('upload prayerathan push')
cliLog('space ' + JSON.stringify({ id: task.id, name: task.name }))

await openOrReuseTab(TRACK, { wait: true, timeout: 30 })
await wait(4)
cliLog(JSON.stringify(await pageInfo()))

const before = await js(`document.body.innerText`)
cliLog(before.match(/Latest release:[\s\S]{0,80}/)?.[0] || before.slice(0, 400))
if (before.includes('Latest release: ' + LABEL)) {
  cliLog('ALREADY_LIVE ' + LABEL)
} else {
  const pos = await js(String.raw`(() => {
    const btn = [...document.querySelectorAll('button')].find(b => (b.innerText||'').trim() === 'Create new release')
    if (!btn) return { missing: true }
    btn.scrollIntoView({ block: 'center' })
    const r = btn.getBoundingClientRect()
    return { x: r.x + r.width/2, y: r.y + r.height/2, disabled: btn.disabled }
  })()`)
  cliLog('create ' + JSON.stringify(pos))
  if (pos.missing) throw new Error('Create new release not found. Sign into Console as mutazyounes@gmail.com')
  await click([Math.round(pos.x), Math.round(pos.y)], { label: 'Create new release' })
  await wait(5)
  cliLog(JSON.stringify(await pageInfo()))

  try {
    await uploadFile('input[accept=".aab"]', AAB)
    cliLog('uploadFile ok')
  } catch (e) {
    cliLog('uploadFile err ' + e.message)
    throw e
  }

  let done = false
  for (let i = 0; i < 15; i++) {
    await wait(6)
    const text = await js(`document.body.innerText`)
    done = text.includes(LABEL) && /app-release\.aab/.test(text)
    cliLog('try ' + i + ' done=' + done)
    if (done) break
  }
  if (!done) throw new Error('Play did not finish processing ' + LABEL)

  const notes = '<en-GB>\n' + NOTES + '\n</en-GB>'
  await fillInput('css:textarea[aria-label="Release notes"]', notes)
  await wait(0.4)

  const next = await js(String.raw`(() => {
    const btn = [...document.querySelectorAll('button')].find(b => (b.innerText||'').trim() === 'Next')
    btn.scrollIntoView({ block: 'center' })
    const r = btn.getBoundingClientRect()
    return { x: r.x + r.width/2, y: r.y + r.height/2, disabled: btn.disabled }
  })()`)
  cliLog('next ' + JSON.stringify(next))
  await click([Math.round(next.x), Math.round(next.y)], { label: 'Next after AAB' })
  await wait(6)

  const pub = await js(String.raw`(() => {
    const btn = [...document.querySelectorAll('button')].find(b => (b.innerText||'').trim() === 'Save and publish')
    if (!btn) return { missing: true }
    btn.scrollIntoView({ block: 'center' })
    const r = btn.getBoundingClientRect()
    return { x: r.x + r.width/2, y: r.y + r.height/2, disabled: btn.disabled }
  })()`)
  cliLog('pub ' + JSON.stringify(pub))
  if (pub.missing) throw new Error('Save and publish missing')
  await click([Math.round(pub.x), Math.round(pub.y)], { label: 'Save and publish' })
  await wait(2)

  const btns = await js(String.raw`(() => {
    return [...document.querySelectorAll('button')].filter(b => (b.innerText||'').includes('Save and publish')).map(b => {
      const r = b.getBoundingClientRect()
      return { text: (b.innerText||'').trim(), x: r.x, y: r.y, w: r.width, h: r.height, vis: r.width > 0 && r.height > 0 }
    })
  })()`)
  const vis = btns.filter(b => b.vis)
  const target = vis[vis.length - 1]
  cliLog('confirm ' + JSON.stringify(target))
  await click([Math.round(target.x + target.w / 2), Math.round(target.y + target.h / 2)], { label: 'confirm publish' })
  await wait(8)

  const after = await js(`document.body.innerText`)
  cliLog(after.match(/Latest release:[\s\S]{0,120}/)?.[0] || after.slice(800, 1600))
  if (!after.includes('Latest release: ' + LABEL)) {
    throw new Error('publish did not land on ' + LABEL)
  }
  cliLog('PUBLISHED ' + LABEL)
}

const done = await completeTaskSpace('upload prayerathan push', { keep: false })
cliLog('complete ' + JSON.stringify(done))
