(async function() {
    let pwd = 'welcome',
        currentUser = {},
        allRouter = {},
        currentRouter = {},
        currentPage = 1,
        currentTotalPage = 0,
        currentFeedId = 0,
        currentMode = '',
        sessionTimer = 0,
        previousKey = '',
        newTitle = '',
        isEOF = false;

    const $ = function(target) {
            return document.getElementById(target)
        },
        inputScreen = $('screen'),
        input = $('text'),
        textarea = $('textarea'),
        promptHead = $('prompthead'),
        inputPrompt = $('prompt'),
        fullLine = '─'.repeat(40),
        isMobile = navigator.userAgent.match(/(iphone|android)/gi);

    const isDoubleChar = (s) => {
            return s.charCodeAt() > 255;
        },
        safe = (s) => {
            return s.replace(/</g, '&lt;', s)
        },
        length2bytes = (d) => {
            return d.split('').map((d) => {
                return isDoubleChar(d) ? 2 : 1
            }).reduce((p, a) => p + a, 0);
        },
        pushState = (url) => {
            if (location.pathname !== url)
                history.pushState(null, null, url);
        },
        touchdownHandler = (e) => {
            if (['TEXTAREA', 'INPUT'].includes(e.target.tagName)) return true;
            e.preventDefault();
            if (currentMode === 'writetext') textarea.focus();
            else input.focus();
            window.scrollTo(0, 0);
        },
        renderTitle = (title) => {
            const titlePadding = ' '.repeat(Math.ceil((60 - length2bytes(title)) / 2)),
                paging = currentRouter.type === 'board' ? (currentPage + '/' + currentTotalPage).padStart(10, ' ') : "";
            return '<b>coroke</b>  <i>' + '─'.repeat(36) + '</i><br />' +
                (pwd !== 'top' ? pwd.toUpperCase().padEnd(10, ' ') : ' '.repeat(10)) + titlePadding + title + titlePadding + paging + '<br />' +
                '<i>' + fullLine + '</i><br />';
        },
        refreshInfo = () => {
            $('info').innerHTML = '<i>' + fullLine + '</i><br />' +
                '명령어안내(C) ' + (currentFeedId === 0 ? (pwd !== 'top' || currentUser !== null ? '초기메뉴(T) ' : '로그인(L) ') : "") +
                (currentRouter.type === 'board' ? '글쓰기(W) 다음(Enter,F) 이전(B) ' : '') +
                (pwd === 'top' ? '직접이동(GO) ' : '') +
                (currentFeedId > 0 ? '글이동(A,N) 목록(P,M)' : ((pwd !== 'top' ? '상위(P,M) ' : '') + '종료(X)')) +
                '<br />';
            setPrompt();
        },
        isNoTitle = (newpwd) => {
            return ['welcome', 'error'].includes(newpwd)
        },
        renderPage = (newpwd, d) => {
            if (d.indexOf('|EOF|') > -1) {
                isEOF = true;
                d = d.replace(/\|EOF\|/g, '')
            } else isEOF = false;
            const allLineChar = '─│┌┐┘└├┬┤┴┼━┃┏┓┛┗┣┳┫┻╋'.split('');
            d = d.split('').map(d => (allLineChar.includes(d) ? '<i>' + d + '</i>' : d)).join('');
            $('prescreen').innerHTML = (!isNoTitle(newpwd) ? renderTitle(allRouter[newpwd].title) : '') +
                d.replace('\n', '<br />');
            if (!isNoTitle(newpwd)) refreshInfo();
        },
        loadCurrentUser = async () => {
                await fetch('/get_user').then(resp => resp.json()).then(d => {
                    currentUser = d;
                    if (Object.keys(currentUser).length > 0 && currentUser.nickname === '') {
                        chooseNickname();
                    } else {
                        loadPage();
                    }
                }).catch(e => {
                    currentUser = null;
                    loadPage();
                });
            },
            loadErrorPage = async () => {
                    await fetch('/txt/error.txt').then(resp => resp.text()).then(d => {
                        $('info').innerHTML = '';
                        renderPage('error', d)
                        pwd = 'error';
                        input.focus();
                    });
                },
                loadWelcome = async () => {
                        await fetch('/txt/welcome.txt').then(resp => resp.text()).then(d => {
                            $('info').innerHTML = '';
                            renderPage('welcome', d)
                            pwd = 'welcome';
                            input.focus();
                        });
                    },
                    loadBoard = async (newpwd, page) => {
                            await fetch('/api/board/' + newpwd + '/' + page).then(resp => resp.text()).then(d => {
                                const paging = d.split('|[PAGE:')[1].split(']')[0].split('/');
                                currentTotalPage = parseInt(paging[1]);
                                currentPage = parseInt(page);
                                pwd = newpwd;
                                currentRouter = allRouter[pwd];
                                currentFeedId = 0;
                                pushState('/' + newpwd)
                                const txt = d.replace(/\|\[PAGE\:(\d+)\/(\d+)\]/gi, '')

                                renderPage(newpwd, txt)
                            });
                        },
                        renderFeed = (d, feedid, page) => {
                            try {
                                currentTotalPage = d.match(/분량:(\d+)페이지/i)[1]
                            } catch (x) {
                                currentTotalPage = 0
                            }
                            currentPage = page;
                            currentFeedId = parseInt(feedid);
                            currentRouter = allRouter[pwd];
                            pushState('/' + pwd + '/' + feedid)
                            renderPage(pwd, d);
                            if (page === 1 && !localStorage.getItem('h' + pwd + feedid)) {
                                localStorage.setItem('h' + pwd + feedid, 1)
                                fetch('/api/board/' + pwd + '/' + feedid + '/hit');
                            }
                        },
                        loadSiblingFeed = async (feedid, direction) => {
                                await fetch('/api/board/' + pwd + '/' + feedid + '/' + direction).then(async (resp) => {
                                    if (resp.status !== 200) return

                                    const text = await resp.text(),
                                        url = resp.url,
                                        feedid = url.split('/')[url.split('/').length - 2];
                                    renderFeed(text, feedid, 1);
                                });
                            },
                            loadFeed = async (feedid, page = 1) => {
                                    await fetch('/api/board/' + pwd + '/' + feedid + '/' + page)
                                        .then(resp => resp.text())
                                        .then(d => {
                                            renderFeed(d, feedid, page)
                                        });
                                },
                                loadPage = async (newpwd) => {
                                        if (!newpwd) {
                                            newpwd = pwd;
                                            if (currentRouter.type === 'board') {
                                                if (currentFeedId > 0) loadFeed(currentFeedId);
                                                else loadBoard(pwd, 1);
                                                return;
                                            }
                                        }
                                        if (newpwd === 'welcome') {
                                            await loadWelcome();
                                            return;
                                        }

                                        const pageType = allRouter[newpwd].type;
                                        currentFeedId = 0

                                        switch (pageType) {
                                            case 'page':
                                                await fetch('/txt/' + newpwd + '.txt').then(resp => resp.text()).then(d => {
                                                    pwd = newpwd;
                                                    currentRouter = allRouter[pwd];
                                                    pushState('/' + newpwd);
                                                    renderPage(newpwd, d)
                                                });
                                                break;
                                            case 'board':
                                                currentPage = 1;
                                                await loadBoard(newpwd, 1);
                                                break;
                                        }
                                    },
                                    scrollDown = () => {
                                        if (isMobile) {
                                            inputScreen.scrollTop = 0;
                                            window.scrollTo(0, 0);
                                        } else
                                            inputScreen.scrollTop = 999999;
                                    },
                                    resizeTextarea = (e) => {
                                        const add = e.type === 'keydown' && e.key === 'Enter' && previousKey !== '.' ? 19 : 0;
                                        textarea.style.minHeight = (textarea.scrollHeight) + add + 'px';
                                        scrollDown();
                                    },
                                    setPrompt = () => {
                                        textarea.value = '';
                                        textarea.style.minHeight = 'inherit';
                                        currentMode = '';
                                        promptHead.innerHTML = '';
                                        inputPrompt.innerHTML = '선택&gt;&nbsp;';
                                        scrollDown();
                                    },
                                    chooseNickname = (retry = false, ovalue = "") => {
                                        currentMode = 'choosenickname';
                                        promptHead.innerHTML = (ovalue !== '' ? '선택&gt; ' + ovalue + '<br /><br />' : '') + (retry ? '사용할 수 없는 이용자명입니다.<br> 다른 ' : '') + '이용자명을 지정해주세요. <br>이용자명은 게시판 등에 표시되며 수정이 불가합니다. 한글 4자 영문 8자 이내<br />';
                                        inputPrompt.innerHTML = '이용자명:&nbsp;'
                                        scrollDown();
                                        input.focus();
                                    },
                                    openWriterTitle = (ovalue) => {
                                        currentMode = 'writetitle';
                                        promptHead.innerHTML = '선택&gt; ' + ovalue + '<br /><br />제목을 입력하세요. 한글 30자 영문 60자 이내<br />';
                                        inputPrompt.innerHTML = '제목:&nbsp;'
                                        scrollDown();
                                        input.focus();
                                    },
                                    openWriterTextarea = (ovalue) => {
                                        currentMode = 'writetext';

                                        newTitle = safe(ovalue.trim());
                                        promptHead.innerHTML += '제목: ' + newTitle + '<br /><br />글을 작성한 후 마지막줄 첫칸에 마침표(.)를 찍고 엔터를 치면 끝납니다<br /><br />';
                                        inputPrompt.innerHTML = ''
                                        input.style.display = 'none';
                                        textarea.value = '';
                                        textarea.style.display = 'block';
                                        scrollDown();
                                        textarea.focus();
                                    },
                                    confirmWriterTextarea = () => {
                                        if (textarea.value.substring(textarea.value.length - 2, textarea.value.length) !== '\n.') return;

                                        promptHead.innerHTML += safe(textarea.value).replace(/\n/g, '<br />') + '<br />'

                                        textarea.value = textarea.value.trim().replace(/\.$/g, '').trim();
                                        textarea.style.display = 'none';
                                        input.style.display = 'block';
                                        inputPrompt.innerHTML = '이 글을 등록하시겠습니까? (Y/n)&nbsp;';
                                        currentMode = 'writeconfirm';
                                        scrollDown();
                                        input.focus();
                                    },
                                    postFeed = async () => {
                                            await fetch('/api/board/' + pwd + '/new', {
                                                method: 'POST',
                                                mode: 'cors',
                                                cache: 'no-cache',
                                                credentials: 'same-origin',
                                                headers: {
                                                    'Content-Type': 'application/json'
                                                },
                                                body: JSON.stringify({
                                                    title: newTitle,
                                                    text: textarea.value
                                                })
                                            }).then(resp => resp.text()).then(d => {
                                                if (d === 'ok') {
                                                    setPrompt();
                                                    loadPage(pwd);
                                                    textarea.value = '';
                                                } else {
                                                    inputPrompt.innerHTML = '등록에 실패했습니다. 다시 시도하시겠습니까? (Y/n)&nbsp;';
                                                    input.focus();
                                                }
                                            });
                                        },
                                        postNickname = async (nickname) => {
                                                await fetch('/api/user/nickname', {
                                                    method: 'POST',
                                                    mode: 'cors',
                                                    cache: 'no-cache',
                                                    credentials: 'same-origin',
                                                    headers: {
                                                        'Content-Type': 'application/json'
                                                    },
                                                    body: JSON.stringify({
                                                        nickname: nickname
                                                    })
                                                }).then(resp => resp.text()).then(d => {
                                                    if (d === 'ok') {
                                                        setPrompt();
                                                        if (pwd === 'welcome') pwd = 'top';
                                                        loadPage(pwd);
                                                    } else {
                                                        chooseNickname(true);
                                                    }
                                                });
                                            },
                                            login = (ovalue, required = false) => {
                                                currentMode = 'login';
                                                promptHead.innerHTML = '선택&gt; ' + ovalue + '<br /><br />' + (required ? '로그인이 필요합니다.' : '로그인 할') + ' 계정을 선택해주세요<br /><br />  1. Twitter<br />  2. Google<br /><br />  0. 취소<br /><br />';
                                                inputPrompt.innerHTML = '어느 계정으로 로그인하시겠습니까? (0-2)&nbsp;'
                                                scrollDown();
                                            },
                                            timer = () => {
                                                const a = new Date();
                                                $('time').innerHTML = String(a.getHours()).padStart(2, '0') + ':' + String(a.getMinutes()).padStart(2, '0') + ':' + String(a.getSeconds()).padStart(2, '0');
                                                sessionTimer++;
                                                const m = Math.floor(sessionTimer / 60) % 60,
                                                    h = Math.floor(sessionTimer / 60 / 60);
                                                $('session_time').innerText = h + ":" + String(m).padStart(2, "0")
                                            },
                                            handleEnter = async (e) => {
                                                e.stopPropagation();

                                                const ovalue = input.value;
                                                let value = ovalue.toLowerCase().trim();
                                                e.target.value = '';

                                                if (currentMode === 'choosenickname') {
                                                    if (ovalue !== '') await postNickname(ovalue);
                                                    else chooseNickname()
                                                    return;
                                                } else if (['welcome', 'error'].includes(pwd)) {
                                                    if (!['s', 'ㄴ'].includes(value)) $('audio').play();
                                                    loadPage('top');
                                                    return;
                                                } else if (currentMode === 'writeconfirm') {
                                                    if (['y', 'ㅛ', ''].includes(value)) await postFeed()
                                                    else if (['n', 'ㅜ'].includes(value)) setPrompt();
                                                    return;
                                                } else if (currentMode === 'writetitle') {
                                                    if (value === '') {
                                                        currentMode = '';
                                                        setPrompt()
                                                        return;
                                                    } else {
                                                        openWriterTextarea(ovalue);
                                                        e.preventDefault();
                                                        return;
                                                    }
                                                } else if (currentMode === 'login') {
                                                    if (['1', '2'].includes(value)) {
                                                        promptHead.innerHTML += inputPrompt.innerHTML + ovalue + '<br /><br />로그인 화면으로 전환됩니다...<br /><br /><br />'
                                                        inputPrompt.innerHTML = '';
                                                        scrollDown();
                                                        localStorage.setItem('next', location.pathname);
                                                    }
                                                    switch (value) {
                                                        case '1':
                                                            location.href = '/login/oauth1/twitter';
                                                            break;
                                                        case '2':
                                                            location.href = '/login/oauth2/authorization/google';
                                                            break;
                                                        case '0':
                                                        case 'n':
                                                            setPrompt();
                                                            break;
                                                    }
                                                    return;
                                                } else if (currentRouter.type === 'board' && (['w', 'ㅈ'].includes(value))) {
                                                    if (!currentUser) {
                                                        login(ovalue, true)
                                                        return;
                                                    } else if (currentUser.role !== 'ROLE_ADMIN' && currentRouter.write_permissions === 'admin') {
                                                        promptHead.innerHTML = "선택&gt; " + ovalue + "<br /><br />글쓰기 권한이 없습니다.<br />"
                                                        return;
                                                    }
                                                    openWriterTitle(ovalue);
                                                    e.preventDefault();
                                                    return false;
                                                } else if (['t', 'ㅅ'].includes(value)) value = 'go top';
                                                else if (['c', 'ㅊ'].includes(value)) value = 'go help';
                                                else if (['p', 'm', 'ㅔ', 'ㅡ'].includes(value)) {
                                                    if (currentFeedId) {
                                                        await loadBoard(pwd, currentPage);
                                                        return;
                                                    } else
                                                        value = 'go ' + (currentRouter.parent || 'top');
                                                } else if (['l', 'ㅣ'].includes(value)) {
                                                    login(ovalue);
                                                    return;
                                                } else if (['x', 'ㅌ'].includes(value)) {
                                                    await fetch('/logout').then(d => d.text()).then(d => {
                                                        if (d === 'ok') {
                                                            inputPrompt.innerHTML = '';
                                                            loadWelcome();
                                                        }
                                                    })
                                                    return;
                                                } else if (currentRouter.type === 'board' && currentFeedId === 0 && ['', 'f', 'ㄹ'].includes(value) && !isEOF) {
                                                    return await loadBoard(pwd, currentPage + 1)
                                                } else if (currentRouter.type === 'board' && currentFeedId === 0 && ['b', 'ㅠ'].includes(value) && currentPage > 1) {
                                                    return await loadBoard(pwd, currentPage - 1)
                                                } else if (currentFeedId > 0 && ['a', 'ㅁ'].includes(value)) {
                                                    return await loadSiblingFeed(currentFeedId, 'forward')
                                                } else if (currentFeedId > 1 && ['n', 'ㅜ'].includes(value)) {
                                                    return await loadSiblingFeed(currentFeedId, 'backward')
                                                } else if (currentFeedId > 0 && ['', 'f', 'ㄹ'].includes(value) && !isEOF) {
                                                    return await loadFeed(currentFeedId, currentPage + 1)
                                                } else if (currentFeedId > 0 && ['b', 'ㅠ'].includes(value) && currentPage > 1) {
                                                    return await loadFeed(currentFeedId, currentPage - 1)
                                                } else if (currentRouter.type === 'board' && !currentFeedId && !isNaN(parseInt(value))) {
                                                    currentPage = 1;
                                                    await loadFeed(value)
                                                    return;
                                                } else if (value === 'set nickname') {
                                                    chooseNickname(false, ovalue);
                                                    return;
                                                }

                                                if (value.substring(0, 2) === 'go') {
                                                    const dest = value.split(' ')[1];
                                                    if (!allRouter[dest]) return;
                                                    await loadPage(dest);
                                                } else {
                                                    if (!currentRouter.routes || !Object.keys(currentRouter.routes).includes(value)) return;
                                                    const newpwd = currentRouter.routes[value];
                                                    await loadPage(newpwd)
                                                }
                                            }

    if ('virtualKeyboard' in navigator) {
        navigator.virtualKeyboard.overlaysContent = true;
    }

    ['touchstart', 'touchend', 'mousedown', 'mouseup'].forEach(d => {
        document.addEventListener(d, touchdownHandler);
    });
    document.addEventListener('keydown', (e) => {
        if (e.isComposing) return true;
        if (e.target.tagName === 'TEXTAREA') return true;

        if (pwd === '') {
            if (e.key === 'Enter')
                loadPage('top');
            else
                input.value = '';
            return;
        } else if (e.key === 'Enter')
            handleEnter(e)
        else if (e.target !== input)
            (currentMode === 'writetext' ? textarea : input).focus();
    });
    textarea.addEventListener('keydown', (e) => {
        if (e.isComposing) return true;

        resizeTextarea(e);
        if (e.key === 'Enter') confirmWriterTextarea()
    })
    textarea.addEventListener('keyup', (e) => {
        resizeTextarea(e);
    });
    input.addEventListener('keyup', async (e) => {
        if (e.isComposing) return true;
    });

    textarea.setAttribute('tabindex', '-1')

    await fetch('/routes.json').then(resp => resp.json()).then(d => {
        allRouter = d;
        Object.entries(d).forEach((v) => {
            if (v[1].routes) //Object.keys(v[1]).includes('routes'))
                Object.values(v[1]['routes']).forEach((v2) => {
                    allRouter[v2]['parent'] = v[0];
                })
        });
        if (d[pwd]) currentRouter = d[pwd];
    })

    window.setInterval(timer, 1000);
    timer();
    if (localStorage.getItem('next')) {
        history.replaceState({}, null, localStorage.getItem('next'));
        localStorage.removeItem('next');
    }

    if (location.pathname !== '/') {
        const pickle = location.pathname.split('/')
        const newpwd = pickle[1];
        if (!Object.keys(allRouter).includes(newpwd)) {
            await loadErrorPage();
        } else {
            pwd = newpwd;

            currentRouter = allRouter[pwd];
            if (currentRouter.type === 'board' && pickle.length > 2) {
                const id = parseInt(pickle[2]);
                if (!isNaN(id)) currentFeedId = id;
            }
        }
    }
    if (pwd !== 'error') await loadCurrentUser();
    input.focus();
})();