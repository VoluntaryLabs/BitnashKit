//
//  AppDelegate.h
//  BitnashKitExample
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <BitnashKit/BitnashKit.h>

@interface AppDelegate : NSObject <NSApplicationDelegate>

@property (assign) IBOutlet NSWindow *window;

@property BNWallet *buyerWallet;
@property BNWallet *sellerWallet;
@property BNEscrowTx *escrowTx;
@property BNReleaseTx *releaseTx;

@property IBOutlet NSTextField *textField;
@property IBOutlet NSImageView *imageView;

@end
