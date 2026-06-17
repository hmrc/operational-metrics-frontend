// Search bar — calls the menu-bar-backend quicksearch endpoint and renders JSON results

const bar = document.getElementById("catalogue-search-bar")
const searchContainer = document.getElementById("catalogue-search-box")
const input = document.getElementById("catalogue-search")
const matches = document.getElementById("catalogue-search-matches")
const mainmenu = document.getElementById("main-menu-bar")

let selectedItem = 0
const minSearchLen = 3

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;")
}

function renderResults(results) {
  if (results.length === 0) {
    return (
      '<table style="width:100%">' +
        '<tbody class="catalogue-search-matches-body">' +
          '<tr><td>No results found</td></tr>' +
        '</tbody>' +
      '</table>'
    )
  }

  const rows = results.map((r, i) => {
    const target =
      r.openInNewWindow
        ? ' target="_blank" rel="noreferrer noopener"'
        : ''

    return (
      '<tr>' +
        '<td><a id="search-item-' + i + '" href="' + escapeHtml(r.href) + '"' + target + '>' +
          escapeHtml(r.name) +
        '</a></td>' +
        '<td><strong class="search-item-type">' + escapeHtml(r.linkType.toLowerCase()) + '</strong></td>' +
      '</tr>'
    )
  }).join('')

  return (
    '<table style="width:100%">' +
      '<tbody class="catalogue-search-matches-body">' + rows + '</tbody>' +
    '</table>'
  )
}

const quickSearchUrl = bar.dataset.quicksearchUrl || "/quicksearch"

let debounceTimer

function search(q) {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(function () {
    const url = new URL(quickSearchUrl, window.location.origin)
    url.searchParams.set("query", q)

    const oReq = new XMLHttpRequest()
    oReq.onreadystatechange = function () {
      if (this.readyState === 4 && this.status === 200) {
        const results = JSON.parse(oReq.responseText)
        matches.innerHTML = renderResults(results)
        selectedItem = 0
        highlight(0)
      }
    }
    oReq.open("GET", url.toString())
    oReq.send()
  }, 250)
}

function toggleSearch() {
  if (searchContainer.classList.contains("search-width-initial")) {
    showSearchBar()
  } else {
    hideSearchBar()
  }
}

function showSearchBar() {
  bar.classList.remove("hidden-for-small-screens")
  searchContainer.classList.remove("search-width-initial")
  searchContainer.classList.add("search-width")
  mainmenu.classList.add("d-none")
  input.focus()
  input.value = ""
}

function hideSearchBar() {
  bar.classList.add("hidden-for-small-screens")
  searchContainer.classList.remove("search-width")
  searchContainer.classList.add("search-width-initial")
  matches.classList.add("d-none")
  mainmenu.classList.remove("d-none")
  input.value = ""
  selectedItem = -1
  matches.innerHTML = ""
}

function globalSearchShortcut(e) {
  if (e.ctrlKey && e.key === " ") {
    toggleSearch()
  }
}

function searchInputListener(e) {
  if (e.keyCode === 13) {
    const firstItem = document.getElementById("search-item-" + selectedItem)
    if (firstItem != null) {
      firstItem.click()
    }
  } else if (e.keyCode === 38) {
    if (selectedItem > 0) {
      unhighlight(selectedItem)
      selectedItem--
      highlight(selectedItem)
    }
  } else if (e.keyCode === 40) {
    unhighlight(selectedItem)
    selectedItem++
    highlight(selectedItem)
  } else if (e.keyCode === 27) {
    hideSearchBar()
  } else if (e.keyCode > 40 || e.keyCode < 33) {
    if (e.target.value.length >= minSearchLen) {
      matches.classList.remove("d-none")
      search(e.target.value)
    } else {
      matches.innerHTML = ""
      matches.classList.add("d-none")
    }
  }
}

function disableArrowKeys(e) {
  switch (e.key) {
    case "ArrowUp":
    case "ArrowDown":
      e.preventDefault()
      break
  }
}

function highlight(pos) {
  const item = document.getElementById("search-item-" + pos)
  if (item != null) {
    item.parentElement.parentElement.classList.add("search-match-selected")
  }
}

function unhighlight(pos) {
  const item = document.getElementById("search-item-" + pos)
  if (item != null) {
    item.parentElement.parentElement.classList.remove("search-match-selected")
  }
}

document.getElementById("searchicon").addEventListener("click", function (e) {
  e.stopImmediatePropagation()
  toggleSearch()
}, true)

document.getElementById("catalogue-search").addEventListener("focus", function (e) {
  e.stopImmediatePropagation()
  showSearchBar()
}, true)

input.addEventListener("keyup", searchInputListener, false)
input.addEventListener("keydown", disableArrowKeys, false)
document.getElementById("standard-layout-container").addEventListener("click", function () { hideSearchBar() }, true)
document.addEventListener("keyup", globalSearchShortcut, false)
