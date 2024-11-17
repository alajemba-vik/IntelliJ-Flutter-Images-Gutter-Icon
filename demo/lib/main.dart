//Flutter app with a screen for settings
import 'package:demo/src/drawables.dart';
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
        appBar: AppBar(
            title: Row(
              children: <Widget>[
                SvgPicture.asset(
                    "assets/images/test_image.svg",
                  width: 50,
                  height: 50,
                ),
                Image.asset(
                    "assets/images/test_image2.png", width: 50, height: 50,
                ),
                SizedBox(width: 8),
                Text('Settings'),
              ],
            )
        ),
        body: SettingsScreen(),
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
      children: <Widget>[
        ListTile(
          leading: Icon(Icons.wifi),
          title: Text('Wi-Fi'),
          subtitle: Text('Connect to networks'),
          trailing: Switch(
            value: true,
            onChanged: (bool value) {},
          ),
        ),
        ListTile(
          leading: Icon(Icons.bluetooth),
          title: Text('Bluetooth'),
          subtitle: Text('Connect to devices'),
          trailing: Switch(
            value: false,
            onChanged: (bool value) {},
          ),
        ),
        ListTile(
          leading: Icon(Icons.data_usage),
          title: Text('Data usage'),
          subtitle: Text('View data usage'),
          trailing: Icon(Icons.arrow_forward),
        ),
      ],
    );
  }
}

