import http from 'k6/http';
import exec from 'k6/execution';
import { check, fail } from 'k6';

const port = `${__ENV.SERVER_PORT}`
const ctxRootPath = `${__ENV.CTX_ROOT_PATH}`

const url = 'http://localhost:' + port + "/" + (ctxRootPath.length > 0 ? ctxRootPath : "task");

export function setup() {
}

export default function () {
    function checkStatus(res) {
        if (check(res, { 'status code MUST be 200': (res) => res.status === 200 }) !== true) {
            fail('error on ' + res.request.method + ' ' + res.url + ', response body "' + res.body + '"' +
                ', status ' + res.status + ', error: ' + res.error);
        }
        return res
    }

    const id = exec.vu.idInTest + "-" + exec.vu.iterationInInstance;
    const res = checkStatus(http.post(url, newTask(id), { headers: { 'Content-Type': 'application/json' } }));

    const getRes = checkStatus(http.get(url + "/" + id));
    let loadedTask = JSON.parse(getRes.body);
    if (!check(loadedTask, { 'unexpected id': (t) => t.id === id })) {
        fail('unexpected id ' + loadedTask.id + ', must be ' + id);
    }
    const delRes = checkStatus(http.del(url + "/" + id));
}

function newTask(id) {
    return JSON.stringify({
        "id": id, "text": "text_" + id, "deadline": new Date().toISOString()
    });
}