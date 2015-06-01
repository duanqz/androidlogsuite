package com.androidlogsuite.model;

import com.androidlogsuite.output.Output;

/**
 * Interface of the model.
 * <p>
 * The responsibility of a model is:
 * <li>parsing the result of a adb command</li>
 * <li>drawing out the result</li>
 * </p>
 *
 * @author duanqizhi
 */
public interface IModel {

    String getAdbCommand();

    void startParsing();

    void stopParsing();

    void draw(Output output);
}
