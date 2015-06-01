package com.androidlogsuite.task;

/**
 * Interface of socket task.
 * <p>
 * Task could interact with the target back and forth:
 * <li>In the phase of transmitting, send data to the target</li>
 * <li>In the phase of receiving, process data from the target</li>
 * </p>
 * @author duanqizhi
 */
public interface ISocketTask extends ITask {

    /**
     * @return Whether start connecting
     */
    boolean connect();

    /**
     * @return Whether succeed to transmit data to the target
     */
    boolean transmit();

    /**
     * @return Whether succeed to receive the data from the target
     */
    boolean receive();

    /**
     * @return Whether succeed to close the connection
     */
    boolean close();

}
