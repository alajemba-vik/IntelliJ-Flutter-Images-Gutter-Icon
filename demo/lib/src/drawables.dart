import 'package:flutter/widgets.dart';
import 'package:flutter_svg/svg.dart';

final class DemoDrawables {
  DemoDrawables._();
  static const testImage = "/assets/images/test_image2.png";
  static const pluginIcon = "/assets/images/test_image.svg";
  static final pluginIconSvg = SvgPicture.asset("/assets/images/test_image.svg");
  static const pluginIconAssetImg = AssetImage('/assets/images/test_image.svg');
}