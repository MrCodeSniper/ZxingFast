/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.google.zxing.multi.qrcode.detector;

import cn.google.zxing.DecodeHintType;
import cn.google.zxing.NotFoundException;
import cn.google.zxing.ReaderException;
import cn.google.zxing.ResultPointCallback;
import cn.google.zxing.common.BitMatrix;
import cn.google.zxing.common.DetectorResult;
import cn.google.zxing.qrcode.detector.Detector;
import cn.google.zxing.qrcode.detector.FinderPatternInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Encapsulates logic that can detect one or more QR Codes in an image, even if the QR Code
 * is rotated or skewed, or partially obscured.</p>
 *
 * @author Sean Owen
 * @author Hannes Erven
 */
public final class MultiDetector extends Detector {

  private static final DetectorResult[] EMPTY_DETECTOR_RESULTS = new DetectorResult[0];

  public MultiDetector(BitMatrix image) {
    super(image);
  }

  public DetectorResult[] detectMulti(Map<DecodeHintType,?> hints) throws NotFoundException {
    BitMatrix image = getImage();
    ResultPointCallback resultPointCallback =
        hints == null ? null : (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
    MultiFinderPatternFinder finder = new MultiFinderPatternFinder(image, resultPointCallback);
    FinderPatternInfo[] infos = finder.findMulti(hints);

    if (infos.length == 0) {
      throw NotFoundException.getNotFoundInstance();
    }

    List<DetectorResult> result = new ArrayList<>();
    for (FinderPatternInfo info : infos) {
      try {
        result.add(processFinderPatternInfo(info));
      } catch (ReaderException e) {
        // ignore
      }
    }
    if (result.isEmpty()) {
      return EMPTY_DETECTOR_RESULTS;
    } else {
      return result.toArray(new DetectorResult[result.size()]);
    }
  }

}