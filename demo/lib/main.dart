//Flutter app with a screen for settings
import 'package:demo/src/drawables.dart';
import 'package:demo/src/images.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

void main() {
  runApp(
    MaterialApp(
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepOrange),
        useMaterial3: true,
      ),
      home: Scaffold(
        body: Column(
          children: [
            SizedBox(height: 100,),
            SettingsScreen(),
          ],
        ),
      ),
    ),
  );
}

// with list tiles that have icons and text and switches
class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      shrinkWrap: true,
      children: <Widget>[
        Row(
          children: <Widget>[
            SizedBox(width: 8),
            Text('Settings'),
          ],
        ),
        ListTile(
          leading: Image.asset("assets/images/test_image2.png"),
          title: Text('Wi-Fi'),
          subtitle: Text('Connect to networks'),
          trailing: Switch(
            value: true,
            onChanged: (bool value) {},
          ),
        ),
        ListTile(
          leading: Image.asset(DemoDrawables.testImage),
          title: Text('Bluetooth'),
          subtitle: Text('Connect to devices'),
          trailing: Switch(
            value: false,
            onChanged: (bool value) {},
          ),
        ),
        ListTile(
          leading: SvgPicture.asset(DemoDrawables.pluginIcon),
          title: Text('Wi-Fi'),
          subtitle: Text('Connect to devices'),
          trailing: Switch(
            value: false,
            onChanged: (bool value) {},
          ),
        ),
        ListTile(
          leading: Image.asset(FirstDemoImages.testImage),
          title: Text('Data usage'),
          subtitle: Text('View data usage'),
          trailing: Icon(Icons.arrow_forward),
        ),
      ],
    );
  }
}

