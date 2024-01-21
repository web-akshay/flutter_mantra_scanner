import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
// import 'package:quick_usb/quick_usb.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  var methodChannel = const MethodChannel('native-channel');
  // List<UsbDevice>? _deviceList = [];
  // Future<void> _getDeviceList() async {
  //   _deviceList = await QuickUsb.getDeviceList();
  //   log('deviceList $_deviceList');
  //   if (_deviceList != null) {
  //     if (_deviceList!.isNotEmpty) {
  //       var hasPermission = QuickUsb.requestPermission(_deviceList![0]);
  //       log('----->hasPermission $hasPermission');
  //     }
  //   }
  // }
  Uint8List? _bytes;
  @override
  void initState() {
    super.initState();
  }

  Future<String> _callNativeCode() async {
    try {
      var data = await methodChannel.invokeMethod('log-message');
      log('message---->$data');
      return data;
    } on PlatformException catch (e) {
      return "Failed to Invoke: '${e.message}'.";
    }
  }

  Future<void> _initScanner() async {
    try {
      var data = await methodChannel.invokeMethod('initScanner');
      log('message---->$data');
    } on PlatformException catch (e) {
      log("Failed to Invoke: '${e.message}'.");
    }
  }

  Future<void> _startScan() async {
    log('message---->mantra-start-scan');
    setState(() {
      _bytes = null;
    });
    try {
      String data = await methodChannel.invokeMethod('mantra-start-scan');

      final imageString = data.replaceAll(RegExp(r"\s+"), "");
      log('image data ---->$imageString');
      log('end image data---->');

      _bytes = base64Decode(imageString);
      // print('_bytes--->$_bytes');
      setState(() {});
    } on PlatformException catch (e) {
      log("Failed to Invoke: '${e.message}'.");
    }
  }

  // Future<void> _getFuture() async {
  //   log('flutter---->get-future');
  //   try {
  //     var data = await methodChannel.invokeMethod('get-future');
  //     log('flutter-future---->$data');
  //   } on PlatformException catch (e) {
  //     log("Failed to Invoke: '${e.message}'.");
  //   }
  // }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mantra flutter demo'),
      ),
      body: Center(
        child: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              // ElevatedButton(
              //     onPressed: () async {
              //       await _callNativeCode();
              //     },
              //     child: const Text('Call Native code')),
              ElevatedButton(
                  onPressed: () async {
                    await _initScanner();
                  },
                  child: const Text('init Scanner')),
              ElevatedButton(
                  onPressed: () async {
                    await _startScan();
                  },
                  child: const Text('start scan')),

              Container(
                  decoration:
                      BoxDecoration(border: Border.all(color: Colors.grey)),
                  height: 200,
                  width: 200,
                  child: _bytes == null
                      ? const Center(child: Text('No data found'))
                      : Image.memory(_bytes!)),

              ElevatedButton(
                  onPressed: () async {
                    _bytes = null;
                    setState(() {});
                  },
                  child: const Text('Reset Data')),
              // ElevatedButton(
              //     onPressed: () async {
              //       await _getFuture();
              //     },
              //     child: const Text('get-future')),
            ],
          ),
        ),
      ),
    );
  }
}
