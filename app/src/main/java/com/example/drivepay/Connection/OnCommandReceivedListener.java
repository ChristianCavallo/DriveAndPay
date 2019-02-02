package com.example.drivepay.Connection;

import SerializedObjects.Command;

public interface OnCommandReceivedListener {

    void OnCommandReceivedCallback(Command cmd);
}
