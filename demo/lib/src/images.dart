final class FirstDemoImages {
  FirstDemoImages._();
  static const base = "/assets/images";
  static const testImage = "$base/test_image2.png";

}
const testImage4 = "${SecondDemoImages.assets}/${SecondDemoImages.drawable}/test_image.png";

final class SecondDemoImages {
  SecondDemoImages._();
  static const assets = "/assets";
  static const drawable = "drawable";
  static const testImage1 = "/assets/drawable/test_image2.svg";
  static const testImage2 = "$assets/$drawable/test_image.png";
}

