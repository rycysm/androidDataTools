package com.android11dome;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.List;
import java.util.Map;

/**
 * 对话框类
 */
class Dialog {
    private  ProgressDialog progressDialog;
    private  boolean is = false;
    private List<Map<String, String>> data;
    private  int InputType = 4;
    private  String OK = "确定";
    private  String CANCEL = "取消";
    private  Context activity;

    public Dialog(Activity activity){
        this.activity=activity;
    }





    public  void showProgressDialog(String msg) {
        progressDialog = ProgressDialog.show(activity, "", msg, true, is);
    }





    public  void dismiss() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

    }




    public  String  RadioDialogBox (Activity activity, String title, String[] items, boolean[] state) {
        AlertDialog.Builder singleBuilder = new AlertDialog.Builder(activity);
        DialogBox db = new DialogBox();
        return db.showDialog(singleBuilder, title, items, state);
    }

    private  class DialogBox {
        private String dialogResult;
        private String[] Items;
        private boolean[] State;
        private Handler mHandler;

        private DialogBox() {
        }

        public String getDialogResult() {
            return this.dialogResult;
        }

        public void setDialogResult(String dialogResult) {
            this.dialogResult = dialogResult;
        }

        public String[] getItems() {
            return this.Items;
        }

        public void setItems(String[] items) {
            this.Items = items;
        }

        public boolean[] getState() {
            return this.State;
        }

        public void setState(boolean[] state) {
            this.State = state;
        }

        public void endDialog(String result) {
            this.setDialogResult(result);
            Message m = this.mHandler.obtainMessage();
            this.mHandler.sendMessage(m);
        }

        public String showDialog(AlertDialog.Builder builder, String title, String[] items, boolean[] state) {
            this.Items = items;
            this.State = state;
            int checkedItem = 0;

            for(int k = 0; k < state.length; ++k) {
                if (state[k]) {
                    checkedItem = k;
                    break;
                }
            }

            builder.setTitle(title).setCancelable(false);
            builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean[] s = DialogBox.this.getState();

                    for(int i = 0; i < s.length; ++i) {
                        if (i == which) {
                            s[i] = true;
                        } else {
                            s[i] = false;
                        }
                    }

                    DialogBox.this.setState(s);
                }
            });
            builder.setPositiveButton(OK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String result = "";
                    boolean[] s = DialogBox.this.getState();
                    String[] it = DialogBox.this.getItems();

                    for(int i = 0; i < s.length; ++i) {
                        if (s[i]) {
                            result = it[i];
                        }
                    }

                    DialogBox.this.endDialog(result);
                }
            });
            builder.setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DialogBox.this.endDialog("");
                }
            });
            builder.show();
            this.mHandler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message mesg) {
                    throw new RuntimeException();
                }
            };

            try {
                Looper.getMainLooper();
                Looper.loop();
            } catch (RuntimeException var7) {
                var7.printStackTrace();
            }

            return this.dialogResult;
        }

        public String showDialog2(AlertDialog.Builder builder, String title, String[] items, boolean[] state) {
            this.Items = items;
            this.State = state;
            builder.setTitle(title).setCancelable(false);
            builder.setMultiChoiceItems(items, state, new DialogInterface.OnMultiChoiceClickListener() {
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    boolean[] s = DialogBox.this.getState();
                    s[which] = isChecked;
                    DialogBox.this.setState(s);
                }
            });
            builder.setPositiveButton(OK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String result = "";
                    boolean[] s = DialogBox.this.getState();
                    String[] it = DialogBox.this.getItems();

                    for(int i = 0; i < s.length; ++i) {
                        if (s[i]) {
                            if (result == "") {
                                result = it[i];
                            } else {
                                result = result + "\n" + it[i];
                            }
                        }
                    }

                    DialogBox.this.endDialog(result);
                }
            });
            builder.setNegativeButton("清空", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DialogBox.this.endDialog("");
                }
            });
            builder.show();
            this.mHandler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message mesg) {
                    throw new RuntimeException();
                }
            };

            try {
                Looper.getMainLooper();
                Looper.loop();
            } catch (RuntimeException var6) {
                var6.printStackTrace();
            }

            return this.dialogResult;
        }
    }



    private  class MessageBox {
        private int dialogResult;
        private Handler mHandler;

        private MessageBox() {
            this.dialogResult = 0;
        }

        public int getDialogResult() {
            return this.dialogResult;
        }

        public void setDialogResult(int dialogResult) {
            this.dialogResult = dialogResult;
        }

        public void endDialog(int result) {
            this.setDialogResult(result);
            Message m = this.mHandler.obtainMessage();
            this.mHandler.sendMessage(m);
        }

        public int showDialog(AlertDialog.Builder builder, String title, String message, String btnOK) {
            builder.setTitle(title).setMessage(message).setCancelable(false).setPositiveButton(btnOK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MessageBox.this.endDialog(0);
                }
            }).show();
            this.mHandler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message mesg) {
                    throw new RuntimeException();
                }
            };

            try {
                Looper.getMainLooper();
                Looper.loop();
            } catch (RuntimeException var6) {
                var6.printStackTrace();
            }

            return this.dialogResult;
        }

        public int showDialog2(AlertDialog.Builder builder, String title, String message, String btnOK, String btnNO) {
            builder.setTitle(title).setMessage(message).setCancelable(false).setPositiveButton(btnOK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MessageBox.this.endDialog(0);
                }
            }).setNegativeButton(btnNO, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MessageBox.this.endDialog(1);
                }
            }).show();
            this.mHandler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message mesg) {
                    throw new RuntimeException();
                }
            };

            try {
                Looper.getMainLooper();
                Looper.loop();
            } catch (RuntimeException var7) {
                var7.printStackTrace();
            }

            return this.dialogResult;
        }
    }

}
