package com.joegec.joycon2android;

interface IUhidService {
    boolean createDevice(int playerIndex, String name);
    boolean sendReport(int playerIndex, in byte[] report);
    void destroyDevice(int playerIndex);
    void destroyAll();
}
