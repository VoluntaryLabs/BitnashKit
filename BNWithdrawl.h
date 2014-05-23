//
//  BNWithdrawl.h
//  BitnashKit
//
//  Created by Steve Dekorte on 5/23/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <NavNodeKit/NavNodeKit.h>

@interface BNWithdrawl : NavInfoNode

@property (strong, nonatomic) NSString *toAddress;
@property (strong, nonatomic) NSString *amountInBtc;
@property (strong, nonatomic) NSString *status;

- (void)send;

@end
