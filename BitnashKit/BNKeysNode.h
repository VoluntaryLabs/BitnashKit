//
//  BNAddressesNode.h
//  BitnashKit
//
//  Created by Rich Collins on 5/2/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <NavKit/NavKit.h>

@class BNWallet;

@interface BNKeysNode : NavInfoNode

@property (weak) BNWallet *wallet;

@end
