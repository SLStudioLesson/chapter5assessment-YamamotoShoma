package com.taskapp.logic;

import java.time.LocalDate;
import java.util.List;

import com.taskapp.dataaccess.LogDataAccess;
import com.taskapp.dataaccess.TaskDataAccess;
import com.taskapp.dataaccess.UserDataAccess;
import com.taskapp.exception.AppException;
import com.taskapp.model.Log;
import com.taskapp.model.Task;
import com.taskapp.model.User;

public class TaskLogic {
    private final TaskDataAccess taskDataAccess;
    private final LogDataAccess logDataAccess;
    private final UserDataAccess userDataAccess;


    public TaskLogic() {
        taskDataAccess = new TaskDataAccess();
        logDataAccess = new LogDataAccess();
        userDataAccess = new UserDataAccess();
    }

    /**
     * 自動採点用に必要なコンストラクタのため、皆さんはこのコンストラクタを利用・削除はしないでください
     * @param taskDataAccess
     * @param logDataAccess
     * @param userDataAccess
     */
    public TaskLogic(TaskDataAccess taskDataAccess, LogDataAccess logDataAccess, UserDataAccess userDataAccess) {
        this.taskDataAccess = taskDataAccess;
        this.logDataAccess = logDataAccess;
        this.userDataAccess = userDataAccess;
    }

    /**
     * 全てのタスクを表示します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findAll()
     * @param loginUser ログインユーザー
     */
    public void showAll(User loginUser) {
        List<Task> tasks = taskDataAccess.findAll();

        tasks.forEach(task -> {
        String statusText = switch(task.getStatus()) {
            case 0 -> "未着手";
            case 1 -> "着手中";
            case 2 -> "完了";
            default -> "不明";
        };
        User taskUser = task.getRepUser();
        String assigneeName = (taskUser != null) ?
                (taskUser.getCode() == loginUser.getCode()) ? "あなたが担当しています" : taskUser.getName() + "が担当しています"
                : "担当者情報なし";

                System.out.println(task.getCode() + ". " + "タスク名：" + task.getName() + " " + assigneeName + ", ステータス：" + statusText);
        });
    }

    /**
     * 新しいタスクを保存します。
     *
     * @see com.taskapp.dataaccess.UserDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#save(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code タスクコード
     * @param name タスク名
     * @param repUserCode 担当ユーザーコード
     * @param loginUser ログインユーザー
     * @throws AppException ユーザーコードが存在しない場合にスローされます
     */
    public void save(int code, String name, int repUserCode,
                    User loginUser) throws AppException {
        User repUser = userDataAccess.findByCode(repUserCode);
        if(repUser == null) {
            throw new AppException("存在するユーザーコードを入力してください");
        }
        Task task = new Task(code, name, 0, repUser);
        taskDataAccess.save(task);

        Log log = new Log(task.getCode(), loginUser.getCode(), 0, LocalDate.now());
        logDataAccess.save(log);

    }

    /**
     * タスクのステータスを変更します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#update(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code タスクコード
     * @param status 新しいステータス
     * @param loginUser ログインユーザー
     * @throws AppException タスクコードが存在しない、またはステータスが前のステータスより1つ先でない場合にスローされます
     */
    public void changeStatus(int code, int status,
                            User loginUser) throws AppException {
        Task task = taskDataAccess.findByCode(code);
            if(task != null) {
            task.setStatus(status);
            taskDataAccess.update(task);
                            
            Log log = new Log(task.getCode(), loginUser.getCode(), status , LocalDate.now());
            logDataAccess.save(log);
        }
    }

    public boolean canChangeStatus(Task task, int newStatus) {
        if(task.getStatus() == 1 && newStatus == 2) {
            return true;
        }
        if(task.getStatus() == 0 && newStatus == 1) {
            return true;
        }
        return false;
    }

    public Task findByCode(int taskCode) {
        return taskDataAccess.findByCode(taskCode);
    }

    /**
     * タスクを削除します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#delete(int)
     * @see com.taskapp.dataaccess.LogDataAccess#deleteByTaskCode(int)
     * @param code タスクコード
     * @throws AppException タスクコードが存在しない、またはタスクのステータスが完了でない場合にスローされます
     */
    // public void delete(int code) throws AppException {
    // }
}