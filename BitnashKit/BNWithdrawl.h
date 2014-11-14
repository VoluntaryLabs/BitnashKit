//
//  BNWithdrawl.h
//  BitnashKit
//
//  Created by Steve Dekorte on 5/23/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <NavNodeKit/NavNodeKit.h>
#import "BNTx.h"

@interface BNWithdrawl : NavInfoNode

@property (strong, nonatomic) NSString *toAddress;
@property (strong, nonatomic) NSString *amountInBtc;
@property (strong, nonatomic) NSString *status;
@property (strong, nonatomic) BNTx *tx;

- (void)send;

@end
