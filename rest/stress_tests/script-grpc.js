import grpc from 'k6/net/grpc';
import exec from 'k6/execution';
import { check, fail } from 'k6';

const port = `${__ENV.SERVER_PORT}`
const rootPath = `${__ENV.SERVICE_ROOT_PATH}`

const url = 'http://localhost:' + port;

const client = new grpc.Client();

client.load(['./go/grpc/proto'], 'google/api/annotations.proto', 'google/api/http.proto', '/task/v1/task.proto');

export function setup() {
}

export default function () {
    function checkStatus(res) {
        if (check(res, { 'status code MUST be grpc.StatusOK ': (res) => res.status === grpc.StatusOK }) !== true) {
            fail('error on ' + res + ', error: ' + res.error);
        }
        return res
    }

    const id = exec.vu.idInTest + "-" + exec.vu.iterationInInstance;

    client.connect(url, {});

    checkStatus(client.invoke('task.v1.TaskService/Store', newTask(id)));

    const getRes = checkStatus(client.invoke('task.v1.TaskService/Get', { "id": id }));

    let loadedTask = JSON.parse(getRes.body);
    if (!check(loadedTask, { 'unexpected id': (t) => t.id === id })) {
        fail('unexpected id ' + loadedTask.id + ', must be ' + id);
    }
    const delRes = checkStatus(client.invoke("task.v1.TaskService/Delete", { "id": id });
}

function newTask(id) {
    return { "id": id, "text": "text_" + id, "deadline": new Date() };
}